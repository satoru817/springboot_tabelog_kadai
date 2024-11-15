package com.example.demo.dto;


import com.example.demo.validation.NoAtSymbol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SignUpForm {
    @NoAtSymbol//メールアドレスとの重複を防ぐためのカスタムアノテーション
    @NotBlank(message="ユーザー名を入力してください。")
    private String name;

    private String nameForReservation;

    private String postalCode;

    private String address;


    private String phoneNumber;

    @NotBlank(message="メールアドレスを入力してください。")
    @Email(message="メールアドレスは正しい形式で入力してください。")
    private String email;

    @NotBlank(message="パスワードを入力してください。")
    @Length(min=8,message="パスワードは8文字以上で入力してください。")
    private String password;

    @NotBlank(message="パスワード（確認用）を入力して下さい。")
    private String passwordConfirmation;

    public Boolean isConfirmed(){
        return this.password.equals(this.passwordConfirmation);
    }

    private MultipartFile icon;//アイコン画像

}
