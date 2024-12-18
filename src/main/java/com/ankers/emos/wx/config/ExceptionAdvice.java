package com.ankers.emos.wx.config;

import com.ankers.emos.wx.exception.EmosException;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public String exceptionHandler(Exception e) {
        log.error("========执行异常========",e);
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            FieldError fieldError = ex.getBindingResult().getFieldError();
            if (fieldError != null) {
                String defaultMessage = fieldError.getDefaultMessage();
                System.out.println("==defaultMessage===>" + defaultMessage);
                return defaultMessage;
            }
            return null;
        } else if (e instanceof EmosException) {
            EmosException ex = (EmosException) e;
            return ex.getMessage();
        } else if (e instanceof UnauthorizedException) {
            return "<<==你不具备相关权限==>>";
        } else {
            return "<<==后端执行异常==>>";
        }
    }
}
