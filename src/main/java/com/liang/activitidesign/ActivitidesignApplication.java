package com.liang.activitidesign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"com.liang","org.activiti"})
@SpringBootApplication
public class ActivitidesignApplication {

	public static void main(String[] args) {
		SpringApplication.run(ActivitidesignApplication.class, args);
	}

}