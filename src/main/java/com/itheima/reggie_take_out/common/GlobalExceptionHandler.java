package com.itheima.reggie_take_out.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * @author Denny
 * @create 2022-08-17 16:20
 */
@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)//异常的类，原本应该从日志中提取
    public R<String> exceptionHandler(SQLIntegrityConstraintViolationException ex)
    {
        if(ex.getMessage().contains("Duplicate entry"))
        {
            String[] s = ex.getMessage().split(" ");
            return R.error(s[2]+"已存在");
        }
        return R.error("未知错误");
    }
}
