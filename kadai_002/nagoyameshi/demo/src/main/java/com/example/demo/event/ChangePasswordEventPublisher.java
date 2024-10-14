package com.example.demo.event;

import com.example.demo.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ChangePasswordEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publishChangePasswordEvent(User user, String requestUrl){
        applicationEventPublisher.publishEvent(new ChangePasswordEvent(this,user,requestUrl));
    }
}
