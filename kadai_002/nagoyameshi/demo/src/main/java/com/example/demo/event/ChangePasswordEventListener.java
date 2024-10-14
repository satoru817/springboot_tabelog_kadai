package com.example.demo.event;

import com.example.demo.entity.User;
import com.example.demo.entity.VerificationToken;
import com.example.demo.service.VerificationTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChangePasswordEventListener {
    private final VerificationTokenService verificationTokenService;
    private final JavaMailSender javaMailSender;

    @EventListener
    private void onChangePasswordEvent(ChangePasswordEvent changePasswordEvent){

        User user = changePasswordEvent.getUser();
        String token = UUID.randomUUID().toString();

        // verificationToken を作成または更新(Upsert)
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationTokenService.save(verificationToken);



        String recipientAddress = user.getEmail();
        String subject = "パスワード変更手続きのご案内";
        String passwordChangeUrl = changePasswordEvent.getRequestUrl() + "/password_change_request?token="+token;
        String message = "以下のリンクをクリックしてパスワード変更を行ってください。";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(recipientAddress);
        mailMessage.setSubject(subject);
        mailMessage.setText(message+"\n"+passwordChangeUrl);
        javaMailSender.send(mailMessage);


    }
}
