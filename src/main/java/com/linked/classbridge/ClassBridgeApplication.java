package com.linked.classbridge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class ClassBridgeApplication {

  public static void main(String[] args) {
    SpringApplication.run(ClassBridgeApplication.class, args);
  }

}
