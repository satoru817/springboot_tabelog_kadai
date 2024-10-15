package com.example.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NoAtSymbolValidator implements ConstraintValidator<NoAtSymbol,String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null){
            return true;
        }

        return !value.contains("@");
    }
}
