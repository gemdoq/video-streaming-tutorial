package com.example.videostreaming.repository;

import com.example.videostreaming.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * =============================================================================
 * VideoRepository - Video 엔티티의 데이터베이스 접근을 담당하는 인터페이스
 * =============================================================================
 *
 * Repository(리포지토리)란?
 * - 데이터베이스와 통신하는 계층입니다
 * - CRUD(Create, Read, Update, Delete) 작업을 수행합니다
 * - Service 계층에서 호출하여 사용합니다
 *
 * JpaRepository란?
 * - Spring Data JPA가 제공하는 인터페이스입니다
 * - 기본적인 CRUD 메서드를 자동으로 제공합니다
 * - 인터페이스만 선언하면 Spring이 자동으로 구현체를 생성해줍니다! (마법!)
 *
 * JpaRepository<Video, Long>의 의미:
 * - 첫 번째 타입 파라미터(Video): 이 리포지토리가 다룰 엔티티 타입
 * - 두 번째 타입 파라미터(Long): 해당 엔티티의 Primary Key 타입
 *
 * 자동으로 제공되는 기본 메서드들:
 * - save(entity): 저장 또는 수정 (INSERT 또는 UPDATE)
 * - findById(id): ID로 조회 (SELECT ... WHERE id = ?)
 * - findAll(): 전체 조회 (SELECT * FROM ...)
 * - deleteById(id): ID로 삭제 (DELETE ... WHERE id = ?)
 * - count(): 전체 개수 조회 (SELECT COUNT(*) FROM ...)
 * - existsById(id): 존재 여부 확인
 */
public interface VideoRepository extends JpaRepository<Video, Long> {

    /**
     * 모든 영상을 생성일시 기준 내림차순으로 조회합니다.
     *
     * Spring Data JPA의 쿼리 메서드 기능:
     * - 메서드 이름만 규칙에 맞게 작성하면 자동으로 쿼리를 생성해줍니다!
     * - SQL을 직접 작성하지 않아도 됩니다
     *
     * 메서드 이름 분석:
     * - findAll: 전체 조회 (SELECT * FROM videos)
     * - By: 조건 시작 (WHERE)
     * - OrderBy: 정렬 (ORDER BY)
     * - CreatedAt: createdAt 필드 기준
     * - Desc: 내림차순 (최신순)
     *
     * 자동 생성되는 SQL:
     * SELECT * FROM videos ORDER BY created_at DESC
     *
     * 다른 예시들:
     * - findByTitle(String title) → SELECT * FROM videos WHERE title = ?
     * - findByTitleContaining(String keyword) → SELECT * FROM videos WHERE title LIKE '%keyword%'
     * - findByFileSizeGreaterThan(Long size) → SELECT * FROM videos WHERE file_size > ?
     *
     * @return 생성일시 내림차순으로 정렬된 영상 목록
     */
    List<Video> findAllByOrderByCreatedAtDesc();
}
