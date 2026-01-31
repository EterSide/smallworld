package com.example.smallworld.dto;

import com.example.smallworld.domain.Card;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * YGOProDeck API 응답 매핑용 DTO
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDto {

    private Long id;
    private String name;
    private String type;
    private String frameType;
    private String desc; // description
    private String race;
    private String attribute;
    private Integer level;
    private Integer atk;
    private Integer def;
    private Integer linkval;

    @JsonProperty("card_images")
    private List<CardImage> cardImages;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CardImage {
        private Long id;
        @JsonProperty("image_url")
        private String imageUrl;
        @JsonProperty("image_url_small")
        private String imageUrlSmall;
    }

    /**
     * DTO를 Entity로 변환
     */
    public Card toEntity() {
        return Card.builder()
                .id(this.id)
                .name(this.name)
                .type(this.type)
                .frameType(this.frameType)
                .race(this.race)
                .attribute(this.attribute)
                .level(this.level)
                .atk(this.atk)
                .def(this.def)
                .linkval(this.linkval)
                .imageUrl(cardImages != null && !cardImages.isEmpty() ? cardImages.get(0).getImageUrlSmall() : null)
                .description(this.desc)
                .build();
    }
}
