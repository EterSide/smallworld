package com.example.smallworld.dto;

import com.example.smallworld.domain.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 연율표고정식 되돌리기 조합
 * 엑시즈 1장 + 융합 1장
 */
@Data
@AllArgsConstructor
public class ReturnCombination {
    private Card xyz;
    private Card fusion;

    /**
     * 별 합계 계산 (랭크 + 레벨)
     */
    public int getTotalStars() {
        int xyzStars = xyz.getStars() != null ? xyz.getStars() : 0;
        int fusionStars = fusion.getStars() != null ? fusion.getStars() : 0;
        return xyzStars + fusionStars;
    }
}
