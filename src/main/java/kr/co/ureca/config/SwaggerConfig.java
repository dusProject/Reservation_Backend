package kr.co.ureca.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {

        Info info = new Info()
                .title("Swagger API")       // API 문서 제목
                .version("v1.0.0")          // API 문서 버전
                .description("좌석 예약 시스템 API 테스트"); // API 문서 설명

        return new OpenAPI()
                .info(info);
    }

}