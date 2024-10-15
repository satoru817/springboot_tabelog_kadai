package com.example.demo.serviceTest;

import com.example.demo.service.PasswordEncryptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncryptionServiceTest {
    private PasswordEncryptionService passwordEncryptionService;

    @BeforeEach
    public void setUp() {
        passwordEncryptionService = new PasswordEncryptionService();
    }

    @Test
    public void testEncryptPassword() {
        String rawPassword = "mySecurePassword";

        // パスワードを暗号化
        String encryptedPassword1 = passwordEncryptionService.encryptPassword(rawPassword);
        String encryptedPassword2 = passwordEncryptionService.encryptPassword(rawPassword);

        // 同じパスワードを暗号化しても、結果が異なることを確認（異なるソルトを使用するため）
        assertNotEquals(encryptedPassword1, encryptedPassword2);
    }

    @Test
    public void testCheckPassword() {
        // 生パスワードのリスト
        List<String> rawPasswordList = List.of("dkajfldsajflksa;da", "fasklfjlkasdk", "dajfkdajoru23485-;kerrrqew90a");

        // 暗号化済みパスワードのリスト
        List<String> encryptedPasswordList = List.of(
                "$2a$10$KcXaEEMqyQW2h0jOwiohdujbbbeyJSTdSjPxnjUpYKcVLXgELzerm",
                "$2a$10$DYuov.y23vuUMWOYzgtyVe77G4MSd4pmSrJpf3mthkD.47HeeZ48S",
                "$2a$10$fKJ.vjhixLq.eMJh7WiUNup3zXTZAljAd34ivSUqeB27q/ex91bfO"
        );

        // 各生パスワードとその対応する暗号化パスワードをチェック
        for (int i = 0; i < rawPasswordList.size(); i++) {
            String rawPassword = rawPasswordList.get(i);
            String encryptedPassword = encryptedPasswordList.get(i);

            // 生パスワードが暗号化されたパスワードと一致することを確認
            assertTrue(passwordEncryptionService.checkPassword(rawPassword, encryptedPassword),
                    "Password matching failed for index " + i);
        }
    }
}
