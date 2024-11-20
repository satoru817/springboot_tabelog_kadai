package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.IpAddressService;
import com.example.demo.service.LoginAttemptService;
import com.example.demo.service.LoginAttemptServiceUsingIpAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private final IpAddressService ipAddressService;
    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;
    private final LoginAttemptServiceUsingIpAddress loginAttemptServiceUsingIpAddress;


    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByNameOrEmail(username,username)
                .orElseThrow(()->new UsernameNotFoundException("User not found: "+username));
        String userRoleName = user.getRole().getName();
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(userRoleName));
        return new UserDetailsImpl(user,authorities);
    }

    public void handleFailedLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //失敗したとき、Sessionの失敗回数の情報とlogin_attemptsテーブルの情報をUpsertする。
        String clientIp = ipAddressService.getClientIP(request);
        loginAttemptService.addUserToLoginAttempt(request);//session情報のUpsert
        loginAttemptServiceUsingIpAddress.loginFailed(clientIp);//IPアドレスに紐づく情報(DB)のUpsert
        if(loginAttemptServiceUsingIpAddress.isBlocked(clientIp)){
            //同一IPアドレスからのアクセスで10回連続で間違えてブロック条件を満たしたらブロック画面にリダイレクト
            response.sendRedirect("/auth/blocked");
        }else if(loginAttemptService.hasExceededMaxAttempts(request)){
            //30分以内に同一ブラウザから5回以上失敗したらパスワード変更画面にリダイレクト
            response.sendRedirect("/passwordChange");
        }else{
            //それ以外ならパスワード入力画面（失敗表示）にリダイレクト
            response.sendRedirect("/auth/login?error=true");
        }


        System.out.println("handleFailedLoginは呼び出されています");
    }

    public void updateUserRolesAndSession(UserDetailsImpl userDetails, HttpServletRequest request) {
        String roleName = null;
        Optional<User> optionalUser = userRepository.findByName(userDetails.getUsername());
        if (optionalUser.isPresent()) {
            roleName = optionalUser.get().getRole().getName(); // DBからロールを取得
            log.info("roleName: {}", roleName);
            userDetails.setUser(optionalUser.get());
        } else {
            log.warn("User not found with username: {}", userDetails.getUsername());
            return; // ユーザーが見つからない場合は処理を中断
        }

        // 新しい権限を作成
        Collection<GrantedAuthority> updatedAuthorities = new ArrayList<>();
        updatedAuthorities.add(new SimpleGrantedAuthority(roleName));

        // Authentication オブジェクトを作成
        Authentication newAuth = new UsernamePasswordAuthenticationToken(userDetails, userDetails.getPassword(), updatedAuthorities);

        // SecurityContextに新しい認証情報を設定
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // セッションの更新
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
            log.info("session情報は更新されました。");
        }
    }


}
