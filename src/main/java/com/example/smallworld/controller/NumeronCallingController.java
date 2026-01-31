package com.example.smallworld.controller;

import com.example.smallworld.domain.Card;
import com.example.smallworld.dto.BanishCombination;
import com.example.smallworld.dto.ReturnCombination;
import com.example.smallworld.service.NumeronCallingService;
import com.example.smallworld.service.YgoprodeckApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 연율표고정식 계산기 컨트롤러
 */
@Controller
@RequestMapping("/numeron")
@RequiredArgsConstructor
@Slf4j
public class NumeronCallingController {

    private final NumeronCallingService numeronService;
    private final YgoprodeckApiService apiService;

    /**
     * 메인 페이지
     */
    @GetMapping
    public String mainPage(Model model) {
        return "numeron";
    }

    /**
     * 카드 검색 API (엑스트라 덱용 - 융합/엑시즈)
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Card>> searchCards(@RequestParam String query) {
        if (query == null || query.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<Card> cards = apiService.searchCards(query).stream()
                .filter(c -> c.isFusionMonster() || c.isXyzMonster())
                .collect(Collectors.toList());

        return ResponseEntity.ok(cards);
    }

    /**
     * 카드 ID 목록으로 조회
     */
    @PostMapping("/api/cards")
    @ResponseBody
    public ResponseEntity<List<Card>> getCardsByIds(@RequestBody List<Long> ids) {
        List<Card> cards = apiService.getCardsByIds(ids);
        return ResponseEntity.ok(cards);
    }

    /**
     * 제외 조합 계산
     */
    @PostMapping("/api/banish")
    @ResponseBody
    public ResponseEntity<List<BanishCombination>> calculateBanish(
            @RequestBody BanishRequest request) {

        List<Card> extraDeck = apiService.getCardsByIds(request.getExtraDeckIds());

        List<BanishCombination> combinations = numeronService.findBanishCombinations(extraDeck,
                request.getTotalCardCount());

        return ResponseEntity.ok(combinations);
    }

    /**
     * 되돌리기 조합 계산
     */
    @PostMapping("/api/return")
    @ResponseBody
    public ResponseEntity<List<ReturnCombination>> calculateReturn(
            @RequestBody ReturnRequest request) {

        List<Card> banishedXyz = apiService.getCardsByIds(request.getBanishedXyzIds());
        List<Card> banishedFusion = apiService.getCardsByIds(request.getBanishedFusionIds());

        List<ReturnCombination> combinations = numeronService.findReturnCombinations(banishedXyz, banishedFusion,
                request.getTargetLevel());

        return ResponseEntity.ok(combinations);
    }

    /**
     * 전체 시퀀스 분석
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public ResponseEntity<NumeronCallingService.NumeronAnalysisResult> analyzeSequence(
            @RequestBody AnalyzeRequest request) {

        List<Card> extraDeck = apiService.getCardsByIds(request.getExtraDeckIds());

        NumeronCallingService.NumeronAnalysisResult result = numeronService.analyzeFullSequence(
                extraDeck,
                request.getTotalCardCount(),
                request.getTargetLevel());

        return ResponseEntity.ok(result);
    }

    /**
     * 가능한 별 합계 범위 조회
     */
    @PostMapping("/api/range")
    @ResponseBody
    public ResponseEntity<int[]> getStarRange(@RequestBody List<Long> extraDeckIds) {
        List<Card> extraDeck = apiService.getCardsByIds(extraDeckIds);
        int[] range = numeronService.getStarSumRange(extraDeck);
        return ResponseEntity.ok(range);
    }

    // Request DTOs
    @lombok.Data
    public static class BanishRequest {
        private List<Long> extraDeckIds;
        private int totalCardCount;
    }

    @lombok.Data
    public static class ReturnRequest {
        private List<Long> banishedXyzIds;
        private List<Long> banishedFusionIds;
        private int targetLevel;
    }

    @lombok.Data
    public static class AnalyzeRequest {
        private List<Long> extraDeckIds;
        private int totalCardCount;
        private int targetLevel;
    }
}
