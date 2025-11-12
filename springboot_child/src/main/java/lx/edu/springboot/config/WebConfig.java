package lx.edu.springboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*") // ✅ 모든 도메인 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")        // ✅ 모든 헤더 허용
                .allowCredentials(true)     // ✅ 쿠키/인증 정보 허용
                .maxAge(3600);              // (선택) Pre-flight 캐시 시간 1시간
    }
}
