package com.easyerp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("Sistema de Gestão")
    String appVersion;
    @Value("ERp Easy Flex Api")
    String appDescription;

    @Bean
    OpenAPI Config() {

        Contact contato = new Contact();
        contato.setEmail("messiasfernandes@gamil.com");
        contato.setName("Messias Fernandes");
        contato.setUrl("https://www.linkedin.com/in/messias-da-consolacao/");
        return new OpenAPI().info(new Info().title("ERp Flex Api").version(appVersion).description(appDescription)
                .termsOfService("http://swagger.io/terms/").contact(contato)
                .license(new License().name("Apache 2.0").url("http://springdoc.org")));
    }
}
