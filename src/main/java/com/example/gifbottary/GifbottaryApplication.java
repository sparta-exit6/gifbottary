package com.example.gifbottary;

import com.example.gifbottary.domain.search.config.PopularSearchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties(PopularSearchProperties.class)
public class GifbottaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GifbottaryApplication.class, args);
    }

}
