package com.example.demo.validation;

import com.example.demo.validation.NoAtSymbolValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @NoAtSymbol アノテーションの定義
@Constraint(validatedBy = NoAtSymbolValidator.class) // バリデータを指定
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface NoAtSymbol {
    String message() default "ユーザー名には@を含めないでください。"; // バリデーションメッセージ
    Class<?>[] groups() default {}; // グループバリデーション
    Class<? extends Payload>[] payload() default {}; // 追加メタデータ
}
