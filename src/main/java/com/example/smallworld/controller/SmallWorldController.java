package com.example.smallworld.controller;

import com.example.smallworld.domain.Card;
import com.example.smallworld.dto.SmallWorldPath;
import com.example.smallworld.service.SmallWorldService;
import com.example.smallworld.service.YgoprodeckApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 스몰월드 계산기 컨트롤러
 */
@Controller
@RequestMapping("/smallworld")
@RequiredArgsConstructor
@Slf4j
public class SmallWorldController {

    private final SmallWorldService smallWorldService;
    private final YgoprodeckApiService apiService;

    /**
     * 메인 페이지
     */
    @GetMapping
    public String mainPage(Model model) {
        return "smallworld";
    }

    /**
     * 카드 검색 API (자동완성용)
     */
    @GetMapping("/api/search")
    @ResponseBody
    public ResponseEntity<List<Card>> searchCards(@RequestParam String query) {
        if (query == null || query.length() < 2) {
            return ResponseEntity.ok(List.of());
        }

        List<Card> cards = apiService.searchMonsters(query);
        return ResponseEntity.ok(cards);
    }

    /**
     * 카드 ID로 조회
     */
    @GetMapping("/api/card/{id}")
    @ResponseBody
    public ResponseEntity<Card> getCard(@PathVariable Long id) {
        return apiService.getCardById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 카드 ID 목록으로 조회 (덱 로딩용)
     */
    @PostMapping("/api/cards")
    @ResponseBody
    public ResponseEntity<List<Card>> getCardsByIds(@RequestBody List<Long> ids) {
        List<Card> cards = apiService.getCardsByIds(ids);
        return ResponseEntity.ok(cards);
    }

    /**
     * 스몰월드 경로 계산
     * 
     * @param request handCardId, targetCardId, deckCardIds를 포함
     */
    @PostMapping("/api/calculate")
    @ResponseBody
    public ResponseEntity<List<SmallWorldPath>> calculatePaths(
            @RequestBody SmallWorldRequest request) {

        // 덱 카드 로드
        List<Card> deck = apiService.getCardsByIds(request.getDeckCardIds())
                .stream()
                .filter(Card::isMonster)
                .collect(Collectors.toList());

        // 패 카드와 타겟 카드 가져오기
        Card handCard = apiService.getCardById(request.getHandCardId()).orElse(null);
        Card targetCard = apiService.getCardById(request.getTargetCardId()).orElse(null);

        if (handCard == null || targetCard == null) {
            return ResponseEntity.badRequest().build();
        }

        List<SmallWorldPath> paths = smallWorldService.findPaths(handCard, deck, targetCard);
        return ResponseEntity.ok(paths);
    }

    /**
     * 특정 패 카드로 서치 가능한 모든 타겟 찾기
     */
    @PostMapping("/api/reachable")
    @ResponseBody
    public ResponseEntity<List<Card>> findReachableTargets(
            @RequestBody ReachableRequest request) {

        List<Card> deck = apiService.getCardsByIds(request.getDeckCardIds())
                .stream()
                .filter(Card::isMonster)
                .collect(Collectors.toList());

        Card handCard = apiService.getCardById(request.getHandCardId()).orElse(null);

        if (handCard == null) {
            return ResponseEntity.badRequest().build();
        }

        List<Card> reachable = smallWorldService.findReachableTargets(handCard, deck);
        return ResponseEntity.ok(reachable);
    }

    /**
     * 덱 전체 분석 (어떤 카드로 어떤 카드를 서치 가능한지)
     */
    @PostMapping("/api/analyze")
    @ResponseBody
    public ResponseEntity<List<DeckAnalysisResult>> analyzeDeck(
            @RequestBody List<Long> deckCardIds) {

        List<Card> deck = apiService.getCardsByIds(deckCardIds)
                .stream()
                .filter(Card::isMonster)
                .collect(Collectors.toList());

        Map<Card, List<Card>> analysis = smallWorldService.analyzeDeck(deck);

        List<DeckAnalysisResult> results = analysis.entrySet().stream()
                .map(e -> new DeckAnalysisResult(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

    // Request/Response DTOs
    @lombok.Data
    public static class SmallWorldRequest {
        private Long handCardId;
        private Long targetCardId;
        private List<Long> deckCardIds;
    }

    @lombok.Data
    public static class ReachableRequest {
        private Long handCardId;
        private List<Long> deckCardIds;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class DeckAnalysisResult {
        private Card handCard;
        private List<Card> reachableTargets;
    }
}
