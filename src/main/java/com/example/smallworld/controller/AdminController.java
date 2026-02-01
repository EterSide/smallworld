package com.example.smallworld.controller;

import com.example.smallworld.service.YgoprodeckApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 관리용 API (첫 로딩·동기화 등)
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final YgoprodeckApiService apiService;

    /**
     * YGOProDeck API에서 전체 카드를 가져와 DB에 저장.
     * PostgreSQL 사용 시 배포 후 한 번만 호출하면 됨 (예: https://smallworld-xxx.onrender.com/admin/sync-cards).
     * 이후에는 슬립에서 깨어나도 DB에 데이터가 남아 있어 검색이 빠름.
     */
    @GetMapping("/sync-cards")
    public ResponseEntity<Map<String, Object>> syncCards() {
        new Thread(() -> {
            try {
                apiService.syncAllCardsFromApi();
            } catch (Exception e) {
                // 로그에 이미 출력됨
            }
        }).start();
        return ResponseEntity.ok(Map.of(
                "message", "전체 카드 동기화를 시작했습니다. 수 분 걸릴 수 있습니다. 로그에서 진행 상황을 확인하세요.",
                "hint", "동기화가 끝나면 검색은 DB만 사용해 빨라집니다."
        ));
    }
}
