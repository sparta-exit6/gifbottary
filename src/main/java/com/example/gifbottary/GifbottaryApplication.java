package com.example.gifbottary;

import com.example.gifbottary.domain.search.config.PopularSearchProperties;
import com.example.gifbottary.infra.dummy.config.SearchDummyDataProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
@EnableConfigurationProperties({PopularSearchProperties.class, SearchDummyDataProperties.class})
public class GifbottaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GifbottaryApplication.class, args);
    }

}
