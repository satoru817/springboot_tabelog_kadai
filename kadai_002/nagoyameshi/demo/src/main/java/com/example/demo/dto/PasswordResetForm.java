package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//MEMO passwordをリセットするときに使うDTO
@Data
public class PasswordResetForm {
    private String verificationToken;

    @NotBlank(message="パスワードを入力してください")
    private String password;

    @NotBlank(message="パスワード（確認用）を入力してください")
    private String passwordConf;

    public Boolean isConfirmed(){
        return this.password.equals(this.passwordConf);
    }
}
