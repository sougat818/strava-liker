package com.github.sougat818.stravaliker.clients;

import com.github.sougat818.stravaliker.dto.AthletesDTO;
import com.github.sougat818.stravaliker.dto.KudosableDTO;
import com.github.sougat818.stravaliker.dto.RelatedActivitiesDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class StravaAPIClient {

    private final WebClient webClient;

    @Value("${token}")
    private String token;

    @Value("${cookie}")
    private String cookie;
    public Flux<AthletesDTO> getRelatedActivities(String activityId) {
        log.info("Crawling {}", activityId);
        return webClient.get()
                .uri(String.format("/feed/activity/%s/group_athletes", activityId))
                .accept(MediaType.APPLICATION_JSON)
                .header("x-csrf-token", token)
                .header("cookie", cookie)
                .retrieve()
                .bodyToMono(RelatedActivitiesDTO.class)
                .flatMapIterable(RelatedActivitiesDTO::getAthletes);
    }

    public Mono<String> kudoActivity(String activityId) {
        log.info("Kudoing {}", activityId);
        return webClient.post()
                .uri(String.format("/feed/activity/%s/kudo", activityId))
                .header("x-csrf-token", token)
                .header("cookie", cookie)
                .retrieve()
                .bodyToMono(String.class)
                .filter(s -> s.contains("true"))
                .thenReturn(activityId);

    }

    public Mono<Boolean> isActivityKudoed(String activityId) {
        log.info("Checking {}", activityId);
        return webClient.get()
                .uri(String.format("/feed/activity/%s/kudos", activityId))
                .header("x-csrf-token", token)
                .header("cookie", cookie)
                .retrieve()
                .bodyToMono(KudosableDTO.class)
                .map(KudosableDTO::getKudosable)
                .map(aBoolean -> !aBoolean);


    }

}
