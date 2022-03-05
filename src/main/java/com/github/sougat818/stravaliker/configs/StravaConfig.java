package com.github.sougat818.stravaliker.configs;


import com.github.sougat818.stravaliker.clients.StravaAPIClient;
import io.netty.handler.logging.LogLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.logging.AdvancedByteBufFormat;

@Configuration
public class StravaConfig {

    @Bean
    public StravaAPIClient stravaAPIClient(){
        HttpClient httpClient = HttpClient
                .create()
                .wiretap("reactor.netty.http.client.HttpClient",
                        LogLevel.DEBUG, AdvancedByteBufFormat.TEXTUAL);
        return new StravaAPIClient(WebClient.builder()
//                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl("https://www.strava.com")
                .build());
    }
}
