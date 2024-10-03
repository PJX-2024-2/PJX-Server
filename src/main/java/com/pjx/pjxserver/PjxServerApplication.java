package com.pjx.pjxserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// 우선 시큐리티 설정 안받음. -> 추후에 설정하고 지워줘야 함!!
@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class PjxServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PjxServerApplication.class, args);
    }

}
