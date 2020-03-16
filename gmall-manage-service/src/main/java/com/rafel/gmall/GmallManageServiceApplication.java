package com.rafel.gmall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication

//MapperScan注解需要导包tk.mybatis.spring.annotation.MapperScan而不是org.mybatis.spring.annotation.MapperScan;
@MapperScan(basePackages = "com.rafel.gmall.manage.mapper")

public class GmallManageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GmallManageServiceApplication.class, args);
	}

}
