// 文件位置：src/main/java/com/community/medical_service/config/CorsConfig.java
package com.community.medical_service.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // 允许的前端地址（根据实际端口修改，Vite 默认 5173）
                .allowedOrigins(
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://127.0.0.1:5173",
                        "http://localhost:3000"  // 备用端口
                )
                // 允许的请求方法
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                // 允许的请求头
                .allowedHeaders("*")
                // 允许携带凭证（Cookie/Authorization）
                .allowCredentials(true)
                // 预检请求缓存时间（秒）
                .maxAge(3600);
    }
}