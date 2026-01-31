package com.example.smallworld.service;

import com.example.smallworld.domain.Card;
import com.example.smallworld.dto.BanishCombination;
import com.example.smallworld.dto.ReturnCombination;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 연율표고정식 계산 서비스
 * 
 * 연율표고정식 조건:
 * 1단계 (제외):
 * - 엑스트라 덱에서 엑시즈 몬스터 2장(같은 랭크) + 융합 몬스터 1장 제외
 * - 세 몬스터의 별(랭크/레벨) 합 = 양쪽 패/필드 카드 수
 * 
 * 2단계 (되돌리기):
 * - 제외된 엑시즈 1장 + 융합 1장을 엑스트라 덱으로 되돌림
 * - 두 몬스터의 별 합 = 상대 몬스터의 별
 * - 성공 시 상대 필드 카드 전부 제외
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NumeronCallingService {

    /**
     * 1단계: 제외 가능한 조합 찾기
     * 
     * @param extraDeck      엑스트라 덱 카드 리스트
     * @param totalCardCount 양쪽 패/필드 카드 총합
     * @return 가능한 제외 조합 목록
     */
    public List<BanishCombination> findBanishCombinations(List<Card> extraDeck, int totalCardCount) {
        // 엑시즈와 융합 분리
        List<Card> xyzCards = extraDeck.stream()
                .filter(Card::isXyzMonster)
                .collect(Collectors.toList());

        List<Card> fusionCards = extraDeck.stream()
                .filter(Card::isFusionMonster)
                .collect(Collectors.toList());

        List<BanishCombination> results = new ArrayList<>();

        // 같은 랭크의 엑시즈 2장 조합 찾기
        for (int i = 0; i < xyzCards.size(); i++) {
            for (int j = i + 1; j < xyzCards.size(); j++) {
                Card xyz1 = xyzCards.get(i);
                Card xyz2 = xyzCards.get(j);

                // 같은 랭크인지 확인
                if (!Objects.equals(xyz1.getLevel(), xyz2.getLevel())) {
                    continue;
                }

                Integer xyzRank = xyz1.getLevel();
                if (xyzRank == null)
                    continue;

                int xyzSum = xyzRank * 2; // 같은 랭크 2장

                // 융합 카드와 조합
                for (Card fusion : fusionCards) {
                    Integer fusionLevel = fusion.getLevel();
                    if (fusionLevel == null)
                        continue;

                    int totalStars = xyzSum + fusionLevel;

                    if (totalStars == totalCardCount) {
                        results.add(new BanishCombination(xyz1, xyz2, fusion));
                    }
                }
            }
        }

        return results;
    }

    /**
     * 2단계: 되돌리기 가능한 조합 찾기
     * 
     * @param banishedXyz    제외된 엑시즈 카드 목록
     * @param banishedFusion 제외된 융합 카드 목록
     * @param targetLevel    상대 몬스터의 레벨/랭크
     * @return 가능한 되돌리기 조합 목록
     */
    public List<ReturnCombination> findReturnCombinations(
            List<Card> banishedXyz,
            List<Card> banishedFusion,
            int targetLevel) {

        List<ReturnCombination> results = new ArrayList<>();

        for (Card xyz : banishedXyz) {
            Integer xyzRank = xyz.getLevel();
            if (xyzRank == null)
                continue;

            for (Card fusion : banishedFusion) {
                Integer fusionLevel = fusion.getLevel();
                if (fusionLevel == null)
                    continue;

                if (xyzRank + fusionLevel == targetLevel) {
                    results.add(new ReturnCombination(xyz, fusion));
                }
            }
        }

        return results;
    }

    /**
     * 전체 분석: 특정 카드 수와 상대 레벨에 대한 모든 가능한 조합
     * 
     * @param extraDeck      엑스트라 덱
     * @param totalCardCount 양쪽 패/필드 카드 총합
     * @param targetLevel    상대 몬스터 레벨 (0이면 무시)
     * @return 분석 결과
     */
    public NumeronAnalysisResult analyzeFullSequence(
            List<Card> extraDeck,
            int totalCardCount,
            int targetLevel) {

        // 1단계: 제외 조합 찾기
        List<BanishCombination> banishCombinations = findBanishCombinations(extraDeck, totalCardCount);

        // 2단계: 각 제외 조합에 대해 되돌리기 조합 찾기
        Map<BanishCombination, List<ReturnCombination>> fullSequences = new LinkedHashMap<>();

        if (targetLevel > 0) {
            for (BanishCombination banish : banishCombinations) {
                // 제외된 카드들
                List<Card> banishedXyz = Arrays.asList(banish.getXyz1(), banish.getXyz2());
                List<Card> banishedFusion = Collections.singletonList(banish.getFusion());

                List<ReturnCombination> returnCombinations = findReturnCombinations(banishedXyz, banishedFusion,
                        targetLevel);

                if (!returnCombinations.isEmpty()) {
                    fullSequences.put(banish, returnCombinations);
                }
            }
        }

        return new NumeronAnalysisResult(banishCombinations, fullSequences);
    }

    /**
     * 가능한 별 합계 범위 계산
     * 엑스트라 덱으로 만들 수 있는 최소/최대 별 합계
     */
    public int[] getStarSumRange(List<Card> extraDeck) {
        List<Card> xyzCards = extraDeck.stream()
                .filter(Card::isXyzMonster)
                .collect(Collectors.toList());

        List<Card> fusionCards = extraDeck.stream()
                .filter(Card::isFusionMonster)
                .collect(Collectors.toList());

        if (xyzCards.size() < 2 || fusionCards.isEmpty()) {
            return new int[] { 0, 0 };
        }

        // 랭크별로 그룹화 (같은 랭크 2장이 필요)
        Map<Integer, Long> rankCounts = xyzCards.stream()
                .filter(c -> c.getLevel() != null)
                .collect(Collectors.groupingBy(Card::getLevel, Collectors.counting()));

        List<Integer> validRanks = rankCounts.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());

        if (validRanks.isEmpty()) {
            return new int[] { 0, 0 };
        }

        // 융합 레벨 범위
        int minFusion = fusionCards.stream()
                .filter(c -> c.getLevel() != null)
                .mapToInt(Card::getLevel)
                .min().orElse(0);

        int maxFusion = fusionCards.stream()
                .filter(c -> c.getLevel() != null)
                .mapToInt(Card::getLevel)
                .max().orElse(0);

        int minRank = validRanks.get(0);
        int maxRank = validRanks.get(validRanks.size() - 1);

        int minSum = (minRank * 2) + minFusion;
        int maxSum = (maxRank * 2) + maxFusion;

        return new int[] { minSum, maxSum };
    }

    /**
     * 분석 결과 DTO
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class NumeronAnalysisResult {
        private List<BanishCombination> banishCombinations;
        private Map<BanishCombination, List<ReturnCombination>> fullSequences;
    }
}
