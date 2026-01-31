package com.example.smallworld.service;

import com.example.smallworld.domain.Card;
import com.example.smallworld.dto.SmallWorldPath;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 스몰월드 계산 서비스
 * 
 * 스몰월드 조건:
 * - 패의 몬스터 1장을 공개하고 뒷면으로 제외
 * - 그 카드와 종족/속성/레벨/공격력/수비력 중 하나만 일치하는 몬스터를 덱에서 보여줌
 * - 덱에서 보여준 몬스터를 뒷면 표시로 제외
 * - 덱에서 보여준 몬스터와 1개만 일치하는 다른 몬스터를 덱에서 패에 넣음
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmallWorldService {

    /**
     * 두 카드가 정확히 1개 파라미터만 일치하는지 확인
     * 비교 항목: 종족(race), 속성(attribute), 레벨(level), 공격력(atk), 수비력(def)
     */
    public boolean hasExactlyOneMatch(Card a, Card b) {
        if (a == null || b == null)
            return false;
        if (!a.isMonster() || !b.isMonster())
            return false;

        int matches = countMatches(a, b);
        return matches == 1;
    }

    /**
     * 두 카드 간의 일치 항목 수 계산
     */
    public int countMatches(Card a, Card b) {
        int matches = 0;

        if (a.getRace() != null && a.getRace().equals(b.getRace()))
            matches++;
        if (a.getAttribute() != null && a.getAttribute().equals(b.getAttribute()))
            matches++;
        if (a.getLevel() != null && a.getLevel().equals(b.getLevel()))
            matches++;
        if (a.getAtk() != null && a.getAtk().equals(b.getAtk()))
            matches++;
        if (a.getDef() != null && a.getDef().equals(b.getDef()))
            matches++;

        return matches;
    }

    /**
     * 일치하는 항목 이름 반환
     */
    public String getMatchingParameter(Card a, Card b) {
        List<String> matches = new ArrayList<>();

        if (a.getRace() != null && a.getRace().equals(b.getRace()))
            matches.add("종족(" + a.getRace() + ")");
        if (a.getAttribute() != null && a.getAttribute().equals(b.getAttribute()))
            matches.add("속성(" + a.getAttribute() + ")");
        if (a.getLevel() != null && a.getLevel().equals(b.getLevel()))
            matches.add("레벨(" + a.getLevel() + ")");
        if (a.getAtk() != null && a.getAtk().equals(b.getAtk()))
            matches.add("공격력(" + a.getAtk() + ")");
        if (a.getDef() != null && a.getDef().equals(b.getDef()))
            matches.add("수비력(" + a.getDef() + ")");

        return String.join(", ", matches);
    }

    /**
     * 특정 패 카드로 서치 가능한 모든 타겟 카드 찾기
     * 
     * @param handCard 패에서 공개할 카드
     * @param deck     덱 리스트 (몬스터만)
     * @return 서치 가능한 타겟 카드 목록
     */
    public List<Card> findReachableTargets(Card handCard, List<Card> deck) {
        Set<Card> reachableTargets = new HashSet<>();

        for (Card bridge : deck) {
            if (bridge.getId().equals(handCard.getId()))
                continue;
            if (!hasExactlyOneMatch(handCard, bridge))
                continue;

            for (Card target : deck) {
                if (target.getId().equals(handCard.getId()))
                    continue;
                if (target.getId().equals(bridge.getId()))
                    continue;
                if (!hasExactlyOneMatch(bridge, target))
                    continue;

                reachableTargets.add(target);
            }
        }

        return new ArrayList<>(reachableTargets);
    }

    /**
     * 패 카드 → 브릿지 → 타겟 경로 찾기
     * 
     * @param handCard   패에서 공개할 카드
     * @param deck       덱 리스트 (몬스터만)
     * @param targetCard 서치하고 싶은 타겟 카드
     * @return 가능한 경로 목록
     */
    public List<SmallWorldPath> findPaths(Card handCard, List<Card> deck, Card targetCard) {
        List<SmallWorldPath> paths = new ArrayList<>();

        for (Card bridge : deck) {
            // 브릿지는 패 카드, 타겟 카드와 달라야 함
            if (bridge.getId().equals(handCard.getId()))
                continue;
            if (bridge.getId().equals(targetCard.getId()))
                continue;

            // 패 카드 → 브릿지: 정확히 1개 일치
            if (!hasExactlyOneMatch(handCard, bridge))
                continue;

            // 브릿지 → 타겟: 정확히 1개 일치
            if (!hasExactlyOneMatch(bridge, targetCard))
                continue;

            SmallWorldPath path = new SmallWorldPath(handCard, bridge, targetCard);
            path.setHandToBridgeMatch(getMatchingParameter(handCard, bridge));
            path.setBridgeToTargetMatch(getMatchingParameter(bridge, targetCard));

            paths.add(path);
        }

        return paths;
    }

    /**
     * 덱에서 브릿지로 사용 가능한 카드 찾기
     * 
     * @param handCard 패에서 공개할 카드
     * @param deck     덱 리스트 (몬스터만)
     * @return 브릿지 카드 목록
     */
    public List<Card> findPossibleBridges(Card handCard, List<Card> deck) {
        return deck.stream()
                .filter(card -> !card.getId().equals(handCard.getId()))
                .filter(card -> hasExactlyOneMatch(handCard, card))
                .collect(Collectors.toList());
    }

    /**
     * 브릿지를 통해 서치 가능한 타겟 찾기
     * 
     * @param bridgeCard 브릿지 카드
     * @param deck       덱 리스트 (몬스터만)
     * @param handCardId 패 카드 ID (제외용)
     * @return 서치 가능한 타겟 목록
     */
    public List<Card> findTargetsFromBridge(Card bridgeCard, List<Card> deck, Long handCardId) {
        return deck.stream()
                .filter(card -> !card.getId().equals(handCardId))
                .filter(card -> !card.getId().equals(bridgeCard.getId()))
                .filter(card -> hasExactlyOneMatch(bridgeCard, card))
                .collect(Collectors.toList());
    }

    /**
     * 덱 전체 분석: 모든 가능한 경로 계산
     */
    public Map<Card, List<Card>> analyzeDeck(List<Card> deck) {
        Map<Card, List<Card>> reachabilityMap = new HashMap<>();

        for (Card handCard : deck) {
            List<Card> reachableTargets = findReachableTargets(handCard, deck);
            reachabilityMap.put(handCard, reachableTargets);
        }

        return reachabilityMap;
    }
}
