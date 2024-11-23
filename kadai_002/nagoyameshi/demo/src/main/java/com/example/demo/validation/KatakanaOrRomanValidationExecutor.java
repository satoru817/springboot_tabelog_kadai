package com.example.demo.validation;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class KatakanaOrRomanValidationExecutor {
    private static final String KATAKANA_PATTERN = "^[ァ-ヶー\\s ]+$";

    private static final String ROMAJI_PATTERN = "^[a-zA-Zz\\s ]+$";

    private static final Pattern KATAKANA_COMPILED = Pattern.compile(KATAKANA_PATTERN);
    private static final Pattern ROMAJI_COMPILED = Pattern.compile(ROMAJI_PATTERN);

    public static boolean isKatakanaAndSpace(String text){

        if(isNullish(text)){
            return false;
        }

        return KATAKANA_COMPILED.matcher(text).matches();
    }

    public static boolean isRomajiAndSpace(String text){
        if(isNullish(text)){
            return false;
        }

        return ROMAJI_COMPILED.matcher(text).matches();
    }

    public static boolean isNullish(String text){
        return (text==null||text.trim().isEmpty());
    }

    public static boolean isKatakanaOrRomanOnly(String text){
        return isKatakanaAndSpace(text)||isRomajiAndSpace(text);
    }
}
