package io.github.alexisTrejo11.construction.company.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI(
        @Value("${APP_NAME:Construction Company API}") String name,
        @Value("${APP_DESCRIPTION:Construction company management API for projects, budgets, expenses, evidence, contractors, and inventory.}") String description,
        @Value("${APP_VERSION:2.0.0}") String version
    ) {
        return new OpenAPI()
                .info(new Info()
                        .title(name)
                        .description(description)
                        .version(version)
                        .contact(new Contact()
                                .name("Codmind")
                                .url("https://codmind.com")
                                .email("apis@codmind.com"))
                        .termsOfService("http://codmind.com/terms")
                );
    }
}
