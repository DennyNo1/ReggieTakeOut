package com.itheima.reggie_take_out;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@Slf4j//由lombok提供，提供log输出
@SpringBootApplication
public class ReggieTakeOutApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReggieTakeOutApplication.class, args);
        log.info("项目启动成功！");
    }

}
