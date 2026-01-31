package com.example.smallworld.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 유희왕 카드 엔티티
 * YGOProDeck API에서 가져온 카드 정보를 저장
 */
@Entity
@Table(name = "cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    private Long id; // 카드 패스코드 (API의 id)

    @Column(nullable = false)
    private String name; // 카드명

    @Column(nullable = false)
    private String type; // Normal Monster, Effect Monster, Fusion Monster, Xyz Monster, Link Monster 등

    private String frameType; // normal, effect, fusion, xyz, link, spell, trap 등

    private String race; // 종족 (Spellcaster, Warrior, Dragon 등) - 스몰월드용

    private String attribute; // 속성 (DARK, LIGHT, WATER, FIRE, WIND, EARTH, DIVINE) - 스몰월드용

    private Integer level; // 레벨 또는 랭크 (엑시즈) - 스몰월드/연율표고정식용

    private Integer atk; // 공격력 - 스몰월드용

    @Column(name = "def_value") // def는 예약어일 수 있음
    private Integer def; // 수비력 - 스몰월드용 (링크 몬스터는 null)

    private Integer linkval; // 링크 값 (링크 몬스터만)

    @Column(length = 1000)
    private String imageUrl; // 카드 이미지 URL

    @Column(length = 2000)
    private String description; // 카드 설명/효과

    /**
     * 몬스터 카드인지 확인
     */
    public boolean isMonster() {
        return type != null && type.contains("Monster");
    }

    /**
     * 엑시즈 몬스터인지 확인
     */
    public boolean isXyzMonster() {
        return "Xyz Monster".equals(type) || "XYZ Monster".equalsIgnoreCase(type);
    }

    /**
     * 융합 몬스터인지 확인
     */
    public boolean isFusionMonster() {
        return "Fusion Monster".equals(type);
    }

    /**
     * 링크 몬스터인지 확인
     */
    public boolean isLinkMonster() {
        return "Link Monster".equals(type);
    }

    /**
     * 별(레벨/랭크/링크값) 반환 - 연율표고정식용
     */
    public Integer getStars() {
        if (isLinkMonster()) {
            return linkval;
        }
        return level;
    }
}
