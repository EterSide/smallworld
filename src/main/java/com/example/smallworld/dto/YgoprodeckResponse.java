package com.example.smallworld.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * YGOProDeck API 응답 wrapper
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class YgoprodeckResponse {

    @JsonProperty("data")
    private List<CardDto> data;
}
