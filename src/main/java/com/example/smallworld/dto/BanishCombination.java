package com.example.smallworld.dto;

import com.example.smallworld.domain.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 연율표고정식 제외 조합
 * 엑시즈 2장 (같은 랭크) + 융합 1장
 */
@Data
@AllArgsConstructor
public class BanishCombination {
    private Card xyz1;
    private Card xyz2;
    private Card fusion;

    /**
     * 별 합계 계산 (랭크 + 랭크 + 레벨)
     */
    public int getTotalStars() {
        int xyz1Stars = xyz1.getStars() != null ? xyz1.getStars() : 0;
        int xyz2Stars = xyz2.getStars() != null ? xyz2.getStars() : 0;
        int fusionStars = fusion.getStars() != null ? fusion.getStars() : 0;
        return xyz1Stars + xyz2Stars + fusionStars;
    }

    /**
     * 엑시즈 랭크 (동일해야 함)
     */
    public Integer getXyzRank() {
        return xyz1.getLevel();
    }
}
