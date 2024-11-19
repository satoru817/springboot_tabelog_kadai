package com.example.demo.controller;

import com.example.demo.dto.PasswordResetForm;
import com.example.demo.dto.SignUpForm;
import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.event.ChangePasswordEventPublisher;
import com.example.demo.repository.LoginAttemptRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.VerificationTokenRepository;
import com.example.demo.security.UserDetailsImpl;
import com.example.demo.security.UserDetailsServiceImpl;
import com.example.demo.service.*;
import com.example.demo.validation.EmailValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Conventions;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final EmailValidator emailValidator;
    private final UserRepository userRepository;
    private final ChangePasswordEventPublisher changePasswordEventPublisher;
    private final VerificationTokenRepository verificationTokenRepository;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncryptionService passwordEncryptionService;
    private final LoginAttemptService loginAttemptService;//Sessionを利用する方
    private final LoginAttemptServiceUsingIpAddress loginAttemptServiceUsingIpAddress;//IPアドレスを利用する方
    private final LoginAttemptRepository loginAttemptRepository;
    private final SignUpFormConverter signUpFormConverter;
    private final JavaMailSender javaMailSender;
    private final UserDetailsServiceImpl userDetailsService;
    private final ImageService imageService;
    private final UserService userService;

    @GetMapping("/auth/admin_add")
    public String addAdmin(Model model){
        SignUpForm signUpForm = new SignUpForm();

        model.addAttribute("signUpForm",signUpForm);
        model.addAttribute("admin",true);

        return "auth/sign_up";
    }

//    @PostMapping("/auth/admin_add")
//    public String registerAdmin(@ModelAttribute @Validated SignUpForm signUpForm,
//                                RedirectAttributes redirectAttributes){
//
//    }



    @GetMapping("/auth/login")
    public String login(Model model,
                        @RequestParam(required = false, name="error")String error,
                        HttpServletRequest request){
        return "auth/login";
    }

    @GetMapping("/passwordChange")
    public String passwordChange(){
        return "auth/password_change_request";
    }

    @GetMapping("/auth/success")
    public String success(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestParam(required = false) String subscription,HttpServletRequest request) {
        // subscription パラメータが存在する場合にのみ処理を行う（upgrade時の処理)
        if (subscription != null) {
            userDetailsService.updateUserRolesAndSession(userDetails,request);
        } else {
            log.info("No subscription parameter found, skipping role update.");
        }

        return "auth/success";
    }


    @PostMapping("/forgot_password")
    public String forgotPassword(RedirectAttributes redirectAttributes,
                                 @RequestParam("email") Optional<String> optionalEmail,
                                 HttpServletRequest httpServletRequest) {
        String requestUrl = httpServletRequest.getRequestURL().toString();
        System.out.println("requestUrl: " + requestUrl);

        if (optionalEmail.isPresent()) {
            String email = optionalEmail.get();
            Optional<User> userOptional = userRepository.findByEmail(email);

            if (userOptional.isPresent()) {
                changePasswordEventPublisher.publishChangePasswordEvent(userOptional.get(), requestUrl);
                System.out.println("イベント発行しています。");
                redirectAttributes.addFlashAttribute("mail_sent", true);
            } else {
                redirectAttributes.addFlashAttribute("mail_not_found", true);
            }
        } else {
            redirectAttributes.addFlashAttribute("mail_not_set", true);
        }

        return "redirect:/passwordChange";
    }


    @GetMapping("/forgot_password/password_change_request")
    public String passwordChange(@RequestParam("token")Optional<String> optionalToken,
                                 RedirectAttributes redirectAttributes,
                                 Model model){
        log.info("token: {}", optionalToken.get());//ここまではおＫ
        String errorMessage;
        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.getVerificationTokenByToken(optionalToken.get());
        if(optionalVerificationToken.isPresent()){
            //30分過ぎているかどうかの判定。

            VerificationToken verificationToken = optionalVerificationToken.get();
            LocalDateTime whenTokenCreated = verificationToken.getUpdatedAt();
            LocalDateTime now = LocalDateTime.now();
            if(whenTokenCreated.isBefore(now.minusMinutes(30))){
                log.info("30分経過しています。");
                errorMessage = "認証メール送信から30分以上経過しています。再度認証メールをお送りください。";
                redirectAttributes.addFlashAttribute("message",errorMessage);
                return "redirect:/passwordChange";

            }else{
                if(!model.containsAttribute("passwordResetForm")){
                    PasswordResetForm passwordResetForm = new PasswordResetForm();
                    passwordResetForm.setVerificationToken(verificationToken.getToken());
                    model.addAttribute("passwordResetForm",passwordResetForm);
                }
                return "auth/password_reset";
            }
        }

        errorMessage = "無効なパスワード変更用URLにアクセスされました。再度パスワード変更をお試しください。";
        redirectAttributes.addFlashAttribute("message",errorMessage);
        return "redirect:/passwordChange";
    }

    @PostMapping("/doChangePassword")
    public String doChangePassword(@ModelAttribute @Validated PasswordResetForm passwordResetForm,
                                   BindingResult bindingResult,
                                   RedirectAttributes redirectAttributes,
                                   Model model){
        //TODO:PasswordResetFormにエラーがあればpasswordChange()に
        // リダイレクトする。この時BindingResultをRedirectAttributeにセットして、
        // リクエストパラメータとしてverificationToken(String)を渡す必要が有
        // る。エラーがなければパスワード更新処理を行う。このときはverificationToken
        // オブジェクトを探し出して、一致するユーザーを選んでパスワードを更新する。

        if(bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("passwordResetForm",passwordResetForm);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
                    + Conventions.getVariableName(passwordResetForm),bindingResult);
            return "redirect:/forgot_password/password_change_request?="+passwordResetForm.getVerificationToken();

        }else{
            if(passwordResetForm.isConfirmed()){
                Optional<VerificationToken> optionalVerificationToken = verificationTokenService.getVerificationTokenByToken(passwordResetForm.getVerificationToken());
                if(optionalVerificationToken.isPresent()){
                    User user = optionalVerificationToken.get().getUser();
                    String rowPassword = passwordResetForm.getPassword();
                    String encPassword = passwordEncryptionService.encryptPassword(rowPassword);
                    user.setPassword(encPassword);
                    userRepository.save(user);
                    model.addAttribute("passwordResetSuccess",true);
                }else{
                    model.addAttribute("passwordResetFailure",true);
                }
                return "auth/login";

            }else{
                redirectAttributes.addFlashAttribute("errorMessage","確認用パスワードが異なります。再度ご確認の上お試しください");
                return "redirect:/forgot_password/password_change_request?="+passwordResetForm.getVerificationToken();
            }
        }

    }



    @GetMapping("/signUp")
    public String signUp(Model model){
        if(!model.containsAttribute("signUpForm")){
            SignUpForm signUpForm = new SignUpForm();
            model.addAttribute("signUpForm",signUpForm);
        }

        return "auth/sign_up";

    }

    @PostMapping("/userRegister")
    public String userRegister(@Validated SignUpForm signUpForm,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model,
                               HttpServletRequest request) throws IOException {
        if(result.hasErrors()){
            redirectAttributes.addFlashAttribute("signUpForm",signUpForm);
            redirectAttributes.addFlashAttribute(BindingResult.MODEL_KEY_PREFIX
                    +Conventions.getVariableName(signUpForm),result);
            return  "redirect:/signUp";
        }else if(!signUpForm.isConfirmed()){

            redirectAttributes.addFlashAttribute("signUpForm",signUpForm);
            redirectAttributes.addFlashAttribute("passwordConfirmationError","パスワード（確認用）が異なっています。");
            return "redirect:/signUp";
        }else{
            Optional<User> optionalUser = signUpFormConverter.singUpFormToUnpaidUser(signUpForm);
            if(optionalUser.isPresent()){
                User user = optionalUser.get();
                user.setEnabled(false);
                userRepository.save(optionalUser.get());
                String token = UUID.randomUUID().toString();

                VerificationToken verificationToken = new VerificationToken();
                verificationToken.setUser(user);
                verificationToken.setToken(token);
                verificationTokenService.save(verificationToken);
                SimpleMailMessage mailMessage = getSimpleMailMessage(request, user, token);
                javaMailSender.send(mailMessage);
                Integer verificationTokenId = verificationToken.getId();
                model.addAttribute("verificationTokenId",verificationTokenId);//再送用に必要な情報
                return "auth/email-verification-sent";
            }else{
                redirectAttributes.addFlashAttribute("signUpForm",signUpForm);
                redirectAttributes.addFlashAttribute("sameUserFoundError","同じメールアドレスかユーザー名のユーザーが見つかりました。");
                return "redirect:/signUp";
            }
        }
    }

    @GetMapping("/resend-verification/{id}")
    public String resendVerification(@PathVariable Integer verificationTokenId,
                                     HttpServletRequest request){
        VerificationToken verificationToken = verificationTokenRepository.getReferenceById(verificationTokenId);
        User user = verificationToken.getUser();
        String newToken = UUID.randomUUID().toString();
        verificationToken.setToken(newToken);
        verificationTokenService.save(verificationToken);//upsert処理
        SimpleMailMessage mailMessage = getSimpleMailMessage(request,user,newToken);
        javaMailSender.send(mailMessage);
        return "auth/verification-email-resent";
    }

    private static SimpleMailMessage getSimpleMailMessage(HttpServletRequest request, User user, String token) {
        String recipientAddress = user.getEmail();
        String subject = "NAGOYAMESHIユーザー認証手続き";
        String passwordChangeUrl = request.getRequestURL() + "/userRegistryConfirmation?token="+ token;
        String message = "以下のリンクをクリックしてあなたのアカウントを有効化してください。";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipientAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message+"\n"+passwordChangeUrl);
        return mailMessage;
    }

    //tokenをつかってverificationToken tableを検索。30分以内なら、ユーザーを有効化する。存在が確認できなければ、無効なことを告げる。
    @GetMapping("/userRegister/userRegistryConfirmation")
    public String confirmUser(@RequestParam("token")String token,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Optional<VerificationToken> optionalVerificationToken = verificationTokenService.getVerificationTokenByToken(token);
        if (optionalVerificationToken.isPresent()) {
            //30分過ぎているかどうかの判定。

            VerificationToken verificationToken = optionalVerificationToken.get();
            LocalDateTime whenTokenCreated = verificationToken.getUpdatedAt();
            LocalDateTime now = LocalDateTime.now();
            if (whenTokenCreated.isBefore(now.minusMinutes(30))) {
                log.info("30分経過しています。");
                String errorMessage = "認証メール送信から30分以上経過しています。再度認証メールをお送りください。";
                redirectAttributes.addFlashAttribute("message", errorMessage);
                return "redirect:/signUp";

            } else {
                User user = verificationToken.getUser();
                user.setEnabled(true);
                userRepository.save(user);
                String userEnabled = "あなたのユーザーアカウントは有効化されました。";
                redirectAttributes.addFlashAttribute("userEnabled",userEnabled);
                return "redirect:/auth/login";
            }
        }else{
            String tokenNotFound = "無効なURLにアクセスされました。再度ユーザー登録を試みて下さい。";
            redirectAttributes.addFlashAttribute("tokenNotFound",tokenNotFound);
            return "redirect:/signUp";
        }


    }

    @Scheduled(cron = "0 0 0 * * ?") // 毎日深夜にlogin_attemptsテーブルをきれいにする
    public void cleanUpOldAttempts () {
        loginAttemptRepository.deleteAttemptsOlderThan(LocalDateTime.now().minusDays(2));
    }

    @GetMapping("/auth/blocked")
    public String blocked () {
        return "auth/blocked";
    }

    //ユーザー情報のアップロード(パスワードはフォームで扱わない。リセットメールを送る形にする。リセットメールを送るリンクを遷移先に作成する）
    @GetMapping("/auth/update")
    public String update(@AuthenticationPrincipal UserDetailsImpl userDetails,
                         Model model){
        User user = userDetails.getUser();
        model.addAttribute("user",user);
        return "auth/edit";
    }

    //名前が重複していないか確認するメソッド
    @PostMapping("/auth/validateName")
    public ResponseEntity<Boolean> validateName(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody Map<String,String> nameMap){
        String name = nameMap.get("name");
        boolean isAvailable;

        if(name.equals(userDetails.getUsername())){
            return ResponseEntity.ok(true);
        }else{
            try{
                isAvailable = !userRepository.existsByName(name);
                return ResponseEntity.ok(isAvailable);
            }catch(Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
            }
        }

    }

    //メールが重複していないか確認するメソッド
    @PostMapping("/auth/validateEmail")
    public ResponseEntity<Boolean> validateEmail(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                @RequestBody Map<String,String> emailMap){
        String email = emailMap.get("email");
        boolean isAvailable;

        if(email.equals(userDetails.getUser().getEmail())){
            return ResponseEntity.ok(true);
        }else{
            try{
                isAvailable = !userRepository.existsByEmail(email);
                return ResponseEntity.ok(isAvailable);
            }catch(Exception e){
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
            }
        }

    }

    //ゆーざー情報編集画面で画像を削除するためのメソッド
    @Transactional
    @DeleteMapping("/api/profile/image")
    public ResponseEntity<String> deleteImage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        User user = userDetails.getUser();

        String fileName = String.format("%s.jpg", userDetails.user.getName());

        boolean deleteSuccess = imageService.deleteImage(fileName);
        user.setProfileImage(null);
        userRepository.save(user);


        if (deleteSuccess) {
            return ResponseEntity.ok("deletion of image success");
        } else {
            return ResponseEntity.internalServerError()
                    .body("deletion of image failed");
        }
    }


    @Transactional
    @PostMapping("/userUpdate")
    public String updateUser(@AuthenticationPrincipal UserDetailsImpl userDetails,
                             @ModelAttribute User user) throws IOException{
        User exUser = userDetails.getUser();
        userService.replaceField(exUser,user);
        userRepository.save(exUser);//upsert
        return "redirect:/auth/update";//編集画面に戻る
    }

}
