package com.example.demo.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class ErrorHandling {

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String  handleException(Exception e){

        //ログにエラー内容を出力
        log.error("エラーが 発生しました。メソッド名:{},例外クラス名:{}",
                e.getStackTrace()[0].getMethodName(),
                e.getClass().getName());
        //エラーページを標示
        return "errorView";
    }
}
