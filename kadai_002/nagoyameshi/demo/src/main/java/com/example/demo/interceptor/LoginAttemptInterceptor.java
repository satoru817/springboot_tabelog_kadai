package com.example.demo.interceptor;

import com.example.demo.service.IpAddressService;
import com.example.demo.service.LoginAttemptService;
import com.example.demo.service.LoginAttemptServiceUsingIpAddress;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@RequiredArgsConstructor
@Component
public class LoginAttemptInterceptor implements HandlerInterceptor {
    private final IpAddressService ipAddressService;
    private final LoginAttemptService loginAttemptService;//session情報を管理する方
    private final LoginAttemptServiceUsingIpAddress loginAttemptServiceUsingIpAddress;//IPアドレス情報を管理する方

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,Object handler) throws Exception{
        String clientIp = ipAddressService.getClientIP(request);

        if(loginAttemptServiceUsingIpAddress.isBlocked(clientIp)||clientIp.equals("unknown")){
            //IPアドレスがブロック対象あるいは、存在しない場合はブロック画面に遷移。
            response.sendRedirect("/auth/blocked");
            return false;
        }else if(loginAttemptService.hasExceededMaxAttempts(request)){
            //同一ブラウザで30分以内に5回以上間違えたら、パスワードリセット画面に遷移
            response.sendRedirect("/passwordChange");
            return false;
        }else{
            return true;//session情報による判定、IPアドレスによる判定両方共OKなら/auth/loginに渡す
        }
    }




}
