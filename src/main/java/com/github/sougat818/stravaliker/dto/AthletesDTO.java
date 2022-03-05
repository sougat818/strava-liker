package com.github.sougat818.stravaliker.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AthletesDTO {

    Long id;

    @JsonProperty("activity_id")
    Long activityId;
}
