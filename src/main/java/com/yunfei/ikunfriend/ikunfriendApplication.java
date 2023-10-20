package com.yunfei.ikunfriend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.yunfei.ikunfriend.mapper")
public class ikunfriendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ikunfriendApplication.class, args);
    }

}
