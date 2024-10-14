package com.example.demo.event;

import com.example.demo.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ChangePasswordEvent extends ApplicationEvent {
    private User user;
    private String requestUrl;
    public ChangePasswordEvent(Object source,User user, String requestUrl) {
        super(source);
        this.requestUrl = requestUrl;
        this.user = user;
    }
}
