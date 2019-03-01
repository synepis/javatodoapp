package io.github.synepis.todo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("UTC"));
    }

//    @Bean
//    public RestTemplate restTemplate() {
//        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
//    }
}
