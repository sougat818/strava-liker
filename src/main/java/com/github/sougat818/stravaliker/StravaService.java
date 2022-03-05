package com.github.sougat818.stravaliker;

import com.github.sougat818.stravaliker.clients.StravaAPIClient;
import com.github.sougat818.stravaliker.dto.AthletesDTO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class StravaService {

    private final StravaAPIClient stravaAPIClient;

    public void findRelatedActivities() {
        stravaAPIClient.getRelatedActivities("6395865500")
                .subscribe(athletesDTO -> log.info("{}", athletesDTO.getActivityId()));
    }

    @SneakyThrows
    @PostConstruct
    public void findEmptyDataFiles() {
        Files.walk(Paths.get("data/")).skip(1).forEach(path -> {
            try {

                Map<String, String> relatedActivities = Files.readAllLines(path).stream()
                        .collect(Collectors.toMap(s -> s.split(",")[0], s -> s.split(",")[1], (s, s2) -> s));
                if (relatedActivities.isEmpty()) {
                    relatedActivities = Map.of(path.getFileName().toString(), "NEW");
                    Map<String, String> finalRelatedActivities = relatedActivities;
                    String mapAsString = relatedActivities.keySet().stream()
                            .map(key -> key + "," + finalRelatedActivities.get(key))
                            .collect(Collectors.joining("\n"));
                    Files.write(path, mapAsString.getBytes());
                }
                int size = relatedActivities.hashCode();
                Map<String, String> finalRelatedActivities = findRelatedActivities(relatedActivities);
                writeToFile(path, finalRelatedActivities);
                while (size != finalRelatedActivities.hashCode()) {
                    size = finalRelatedActivities.hashCode();
                    finalRelatedActivities = findRelatedActivities(relatedActivities);
                    writeToFile(path, finalRelatedActivities);
                }

                finalRelatedActivities = likeActivities(relatedActivities);
                writeToFile(path, finalRelatedActivities);
                while (size != finalRelatedActivities.hashCode()) {
                    size = finalRelatedActivities.hashCode();
                    finalRelatedActivities = likeActivities(relatedActivities);
                    writeToFile(path, finalRelatedActivities);
                }

                finalRelatedActivities = checkActivities(relatedActivities);
                writeToFile(path, finalRelatedActivities);
                while (size != finalRelatedActivities.hashCode()) {
                    size = finalRelatedActivities.hashCode();
                    finalRelatedActivities = checkActivities(relatedActivities);
                    writeToFile(path, finalRelatedActivities);
                }

            } catch (IOException e) {
                log.error("", e);
            }
        });
        log.info("FINISHED");

    }


    @SneakyThrows
    public void writeToFile(Path path, Map<String, String> allActivities) {
        String mapAsString = allActivities.keySet().stream().sorted()
                .map(key -> key + "," + allActivities.get(key))
                .collect(Collectors.joining("\n"));
        Files.write(path, mapAsString.getBytes());
    }

    public Map<String, String> likeActivities(Map<String, String> allActivities) {
        Optional<Map.Entry<String, String>> any = allActivities.entrySet().stream()
                .filter(e -> "CRAWLED".equals(e.getValue()))
                .findAny();
        any.ifPresent(stringStringEntry -> stravaAPIClient.kudoActivity(stringStringEntry.getKey()).mapNotNull(s -> allActivities.replace(String.valueOf(s), "LIKED")).block());
        return allActivities;
    }

    public Map<String, String> checkActivities(Map<String, String> allActivities) {
        Optional<Map.Entry<String, String>> any = allActivities.entrySet().stream()
                .filter(e -> "LIKED".equals(e.getValue()))
                .findAny();
        any.ifPresent(stringStringEntry -> stravaAPIClient.isActivityKudoed(stringStringEntry.getKey()).map(aBoolean -> {
            if(aBoolean){
                allActivities.replace(stringStringEntry.getKey(),"CHECKED");
            }else{
                allActivities.replace(stringStringEntry.getKey(),"CRAWLED");
            }
            return aBoolean;
        }).block());
        return allActivities;
    }


    public Map<String, String> findRelatedActivities(Map<String, String> allActivities) {
        Optional<Map.Entry<String, String>> any = allActivities.entrySet().stream()
                .filter(e -> "NEW".equals(e.getValue()))
                .findAny();
        if (any.isPresent()) {
            stravaAPIClient.getRelatedActivities(any.get().getKey()).map(AthletesDTO::getActivityId).collectList().block()
                    .forEach(activity -> allActivities.putIfAbsent(String.valueOf(activity), "NEW"));
            allActivities.replace(any.get().getKey(), "CRAWLED");
        }

        return allActivities;
    }
}
