package com.example.ums;

import org.springframework.boot.SpringApplication;

public class TestUmsApplication {

	public static void main(String[] args) {
		SpringApplication.from(UmsApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
