package com.example.demo.Util;

public class UtilForString {
    public static boolean isNullOrEmpty(String str){
        return str == null || str.trim().isEmpty();
    }

    public static boolean isValid(String str){
        return !isNullOrEmpty(str);
    }
}
