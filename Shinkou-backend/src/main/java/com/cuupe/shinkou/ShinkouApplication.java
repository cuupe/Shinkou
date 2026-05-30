package com.cuupe.shinkou;

import com.cuupe.shinkou.config.AuthProperties;
import com.cuupe.shinkou.security.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AuthProperties.class,
        JwtProperties.class
})
public class ShinkouApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShinkouApplication.class, args);
    }

}
