package com.example.smallworld.dto;

import com.example.smallworld.domain.Card;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 스몰월드 경로 (패 카드 → 브릿지 → 타겟)
 */
@Data
@AllArgsConstructor
public class SmallWorldPath {
    private Card handCard; // 패에서 공개할 카드
    private Card bridgeCard; // 덱에서 보여줄 브릿지 카드
    private Card targetCard; // 최종 서치할 카드

    /**
     * 매칭 정보 표시용
     */
    private String handToBridgeMatch; // 패→브릿지 일치 항목
    private String bridgeToTargetMatch; // 브릿지→타겟 일치 항목

    public SmallWorldPath(Card handCard, Card bridgeCard, Card targetCard) {
        this.handCard = handCard;
        this.bridgeCard = bridgeCard;
        this.targetCard = targetCard;
    }
}
