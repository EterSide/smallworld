package com.example.smallworld.repository;

import com.example.smallworld.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 카드 Repository
 */
@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    /**
     * 카드명으로 검색 (부분 일치)
     */
    List<Card> findByNameContainingIgnoreCase(String name);

    /**
     * 몬스터 카드만 조회 (스몰월드용)
     */
    @Query("SELECT c FROM Card c WHERE c.type LIKE '%Monster%'")
    List<Card> findAllMonsters();

    /**
     * 엑시즈 몬스터만 조회 (연율표고정식용)
     */
    List<Card> findByType(String type);

    /**
     * 특정 타입들의 카드 조회
     */
    List<Card> findByTypeIn(List<String> types);

    /**
     * ID 목록으로 카드 조회 (덱 로딩용)
     */
    List<Card> findByIdIn(List<Long> ids);

    /**
     * 카드 존재 여부 확인
     */
    boolean existsByName(String name);
}
