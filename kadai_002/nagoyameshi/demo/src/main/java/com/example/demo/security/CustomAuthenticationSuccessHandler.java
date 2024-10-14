package com.example.demo.security;

import com.example.demo.service.IpAddressService;
import com.example.demo.service.LoginAttemptService;
import com.example.demo.service.LoginAttemptServiceUsingIpAddress;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    private final LoginAttemptServiceUsingIpAddress loginAttemptServiceUsingIpAddress;
    private final LoginAttemptService loginAttemptService;
    private final IpAddressService ipAddressService;

    //TODO:sessionとlogin_attemptsの情報を削除しないといけない。->DONE
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // IPアドレスに紐づくログイン失敗情報を削除
        String clientIp = ipAddressService.getClientIP(request);
        loginAttemptServiceUsingIpAddress.loginSucceeded(clientIp);

        //セッションのログイン失敗情報を削除
        loginAttemptService.resetLoginAttempt(request);


        // ログ記録
        log.info("User {} logged in successfully", authentication.getName());

        // リダイレクト
        response.sendRedirect("/auth/success");
    }
}
