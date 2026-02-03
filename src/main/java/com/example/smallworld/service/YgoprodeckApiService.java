package com.example.smallworld.service;

import com.example.smallworld.domain.Card;
import com.example.smallworld.dto.CardDto;
import com.example.smallworld.dto.YgoprodeckResponse;
import com.example.smallworld.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * YGOProDeck API 연동 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class YgoprodeckApiService {

    private final CardRepository cardRepository;
    private final RestTemplate restTemplate;

    @Value("${ygoprodeck.api.base-url}")
    private String apiBaseUrl;

    /**
     * 카드명으로 검색 (fuzzy search)
     * 먼저 DB에서 검색하고, 없으면 API 호출
     */
    @Transactional
    public List<Card> searchCards(String name) {
        // 먼저 로컬 DB에서 검색
        List<Card> localCards = cardRepository.findByNameContainingIgnoreCase(name);
        if (!localCards.isEmpty()) {
            return localCards;
        }

        // API 호출
        try {
            var uri = UriComponentsBuilder.fromUriString(apiBaseUrl + "/cardinfo.php")
                    .queryParam("fname", name)
                    .build()
                    .toUri();

            log.info("Calling YGOProDeck API: {}", uri);
            YgoprodeckResponse response = restTemplate.getForObject(uri, YgoprodeckResponse.class);

            if (response != null && response.getData() != null) {
                List<Card> cards = response.getData().stream()
                        .map(CardDto::toEntity)
                        .collect(Collectors.toList());

                // DB에 저장 (캐싱)
                cardRepository.saveAll(cards);
                return cards;
            }
        } catch (Exception e) {
            log.error("Failed to fetch cards from API: {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    /**
     * 카드 ID로 조회
     */
    @Transactional
    public Optional<Card> getCardById(Long id) {
        // 먼저 로컬 DB에서 검색
        Optional<Card> localCard = cardRepository.findById(id);
        if (localCard.isPresent()) {
            return localCard;
        }

        // API 호출
        try {
            var uri = UriComponentsBuilder.fromUriString(apiBaseUrl + "/cardinfo.php")
                    .queryParam("id", id)
                    .build()
                    .toUri();

            log.info("Calling YGOProDeck API: {}", uri);
            YgoprodeckResponse response = restTemplate.getForObject(uri, YgoprodeckResponse.class);

            if (response != null && response.getData() != null && !response.getData().isEmpty()) {
                Card card = response.getData().get(0).toEntity();
                cardRepository.save(card);
                return Optional.of(card);
            }
        } catch (Exception e) {
            log.error("Failed to fetch card from API: {}", e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * 여러 카드 ID로 조회 (덱 로딩용)
     */
    @Transactional
    public List<Card> getCardsByIds(List<Long> ids) {
        List<Card> existingCards = cardRepository.findByIdIn(ids);

        // 이미 모든 카드가 DB에 있으면 반환
        if (existingCards.size() == ids.size()) {
            return existingCards;
        }

        // 없는 카드들만 API로 조회
        List<Long> existingIds = existingCards.stream()
                .map(Card::getId)
                .collect(Collectors.toList());

        List<Long> missingIds = ids.stream()
                .filter(id -> !existingIds.contains(id))
                .collect(Collectors.toList());

        for (Long id : missingIds) {
            getCardById(id);
        }

        return cardRepository.findByIdIn(ids);
    }

    /**
     * 몬스터 카드만 검색 (스몰월드용)
     */
    @Transactional
    public List<Card> searchMonsters(String name) {
        return searchCards(name).stream()
                .filter(Card::isMonster)
                .collect(Collectors.toList());
    }

    /**
     * YGOProDeck API에서 전체 카드를 가져와 DB에 저장 (PostgreSQL 첫 로딩용).
     * 한 번 호출해 두면 이후 검색은 DB만 사용.
     */
    @Transactional
    public int syncAllCardsFromApi() {
        String url = apiBaseUrl + "/cardinfo.php";
        log.info("Syncing all cards from API: {}", url);
        try {
            YgoprodeckResponse response = restTemplate.getForObject(url, YgoprodeckResponse.class);
            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                log.warn("No cards returned from API");
                return 0;
            }
            List<Card> cards = response.getData().stream()
                    .map(CardDto::toEntity)
                    .collect(toList());
            int batchSize = 500;
            int saved = 0;
            for (int i = 0; i < cards.size(); i += batchSize) {
                int end = Math.min(i + batchSize, cards.size());
                List<Card> batch = cards.subList(i, end);
                cardRepository.saveAll(batch);
                saved += batch.size();
                log.info("Saved cards {}-{} / {}", i + 1, end, cards.size());
            }
            log.info("Sync complete: {} cards saved", saved);
            return saved;
        } catch (Exception e) {
            log.error("Failed to sync cards from API: {}", e.getMessage(), e);
            throw new RuntimeException("전체 카드 동기화 실패: " + e.getMessage(), e);
        }
    }
}
