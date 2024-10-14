package com.example.demo.controller;

import com.example.demo.dto.PasswordResetForm;
import com.example.demo.dto.SignUpForm;
import com.example.demo.entity.LoginAttempt;
import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.event.ChangePasswordEventPublisher;
import com.example.demo.repository.LoginAttemptRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.*;
import com.example.demo.validator.EmailValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Conventions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AuthController {
    private final EmailValidator emailValidator;
    private final UserRepository userRepository;
    private final ChangePasswordEventPublisher changePasswordEventPublisher;
    private final VerificationTokenService verificationTokenService;
    private final PasswordEncryptionService passwordEncryptionService;
    private final LoginAttemptService loginAttemptService;//Sessionを利用する方
    private final LoginAttemptServiceUsingIpAddress loginAttemptServiceUsingIpAddress;//IPアドレスを利用する方
    private final LoginAttemptRepository loginAttemptRepository;


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
    public String success(){
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
        if(optionalToken.isPresent()){
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

        }
        errorMessage = "無効なパスワード変更用URLにアクセスされました。再度パスワード変更をお試しください。";
        redirectAttributes.addFlashAttribute("message",errorMessage);
        return "redirect:/password_change";
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
        SignUpForm signUpForm = new SignUpForm();
        model.addAttribute("signUpForm",signUpForm);
        return "auth/sign_up";

    }



    @Scheduled(cron = "0 0 0 * * ?") // 毎日深夜に実行。
    public void cleanUpOldAttempts() {
        loginAttemptRepository.deleteAttemptsOlderThan(LocalDateTime.now().minusDays(2));
    }

    @GetMapping("/auth/blocked")
    public String blocked(){
        return "auth/blocked";
    }



}
