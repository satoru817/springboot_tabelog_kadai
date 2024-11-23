package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
//予約者名はソートしやすくしたいので、ローマ字と空白のみあるいは、カタカナと空白文字のみを許すようにする。
public class KatakanaOrRomanOnlyValidator implements ConstraintValidator<KatakanaOrRomanOnly,String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context){

        return KatakanaOrRomanValidationExecutor.isKatakanaOrRomanOnly(value);
    }
}
