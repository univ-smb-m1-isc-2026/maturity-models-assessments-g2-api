package fr.univ_smb.info803.maturitymodelsassessmentsapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry){
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:4200", "http://mmag2.oups.net")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS");
    }
}
