package com.example.demo.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {
    private static final String LOGIN_ATTEMPT_KEY = "loginAttemptKey";
    private static final String LAST_ATTEMPT_TIME_KEY = "lastAttemptTimeKey";
    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_TIME_DURATION = 30 * 60 * 1000;//30分

    public void addUserToLoginAttempt(HttpServletRequest request){
        HttpSession session = request.getSession();
        Integer attempts = (Integer) session.getAttribute(LOGIN_ATTEMPT_KEY);
        attempts = (attempts == null)?1:attempts+1;
        session.setAttribute(LOGIN_ATTEMPT_KEY,attempts);
        session.setAttribute(LAST_ATTEMPT_TIME_KEY,System.currentTimeMillis());
    }

    public void resetLoginAttempt(HttpServletRequest request){
        HttpSession session = request.getSession();
        session.removeAttribute(LOGIN_ATTEMPT_KEY);
        session.removeAttribute(LAST_ATTEMPT_TIME_KEY);
    }

    public boolean hasExceededMaxAttempts(HttpServletRequest request){
        HttpSession session = request.getSession();
        Integer attempts = (Integer) session.getAttribute(LOGIN_ATTEMPT_KEY);
        Long lastAttemptTime = (Long)session.getAttribute(LAST_ATTEMPT_TIME_KEY);

        //最後のログインから30分を超えたらリセットする
        if(lastAttemptTime != null && (System.currentTimeMillis()-lastAttemptTime > LOCK_TIME_DURATION)){
            resetLoginAttempt(request);
            return false;
        }

        //30分を超えないうちに最大試行回数を超える回数試みたか？
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }
}
