package com.lz.vhr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.lz.vhr.mapper")
public class VhrSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(VhrSpringbootApplication.class, args);
    }

}
