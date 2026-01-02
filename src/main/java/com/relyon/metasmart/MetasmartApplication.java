package com.relyon.metasmart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MetasmartApplication {

	public static void main(String[] args) {
		SpringApplication.run(MetasmartApplication.class, args);
	}

}
