// 文件位置：src/main/java/com/community/medical_service/MedicalServiceApplication.java
package com.community.medical_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 开启定时任务
public class MedicalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalServiceApplication.class, args);
    }
}