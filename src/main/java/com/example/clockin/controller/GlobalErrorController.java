package com.example.clockin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
@Controller
public class GlobalErrorController {

    // 處理 403 錯誤
    @GetMapping("/403")
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String accessDenied() {
        return "error/403"; // 指向 403 頁面的模板
    }

    // 處理 404 錯誤
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound() {
        return "error/404"; // 指向 404 頁面的模板
    }

    // 處理其他異常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleOtherErrors() {
        return "error/500"; // 指向 500 錯誤頁面的模板
    }
}
