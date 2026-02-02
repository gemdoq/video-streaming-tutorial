package com.example.videostreaming.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * Video - 영상 정보를 저장하는 엔티티 클래스
 * =============================================================================
 *
 * 엔티티(Entity)란?
 * - 데이터베이스의 테이블과 매핑되는 Java 클래스입니다
 * - 이 클래스의 객체 하나 = 테이블의 행(row) 하나
 * - 클래스의 필드 = 테이블의 컬럼(column)
 *
 * 예시:
 * Video 객체 하나를 저장하면 → videos 테이블에 한 행이 추가됩니다
 *
 * JPA (Java Persistence API)란?
 * - Java 객체와 데이터베이스 테이블을 연결해주는 기술입니다
 * - SQL을 직접 작성하지 않아도 객체를 저장/조회할 수 있습니다
 * - 예: videoRepository.save(video) → INSERT 쿼리 자동 생성
 */
@Entity  // 이 클래스가 JPA 엔티티임을 나타냅니다. DB 테이블과 매핑됩니다.
@Table(name = "videos")  // 매핑될 테이블 이름을 지정합니다. 생략하면 클래스명(Video)이 테이블명이 됩니다.
@Getter  // Lombok: 모든 필드의 getter 메서드를 자동 생성합니다 (getId(), getTitle() 등)
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // Lombok: 기본 생성자를 생성합니다. JPA는 기본 생성자가 필수입니다.
public class Video {

    /**
     * 영상의 고유 식별자 (Primary Key)
     *
     * @Id: 이 필드가 테이블의 Primary Key(기본키)임을 나타냅니다
     * @GeneratedValue: 값을 자동으로 생성합니다
     * - IDENTITY 전략: 데이터베이스가 자동으로 1, 2, 3... 순서대로 번호를 부여합니다
     *   (MySQL의 AUTO_INCREMENT, H2의 IDENTITY와 같은 기능)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 영상 제목
     *
     * @Column: 컬럼의 세부 설정을 지정합니다
     * - nullable = false: NULL 값을 허용하지 않음 (필수 입력)
     *   → 데이터베이스에 NOT NULL 제약조건이 추가됩니다
     */
    @Column(nullable = false)
    private String title;

    /**
     * 영상 설명
     *
     * length = 1000: 최대 1000자까지 저장 가능
     * nullable은 생략하면 기본값 true (NULL 허용)
     */
    @Column(length = 1000)
    private String description;

    /**
     * 원본 파일명
     *
     * 사용자가 업로드한 파일의 원래 이름입니다.
     * 예: "my_video.mp4"
     */
    @Column(nullable = false)
    private String fileName;

    /**
     * 저장된 파일명
     *
     * 서버에 실제로 저장되는 파일명입니다.
     * 파일명 중복을 방지하기 위해 UUID로 생성합니다.
     * 예: "550e8400-e29b-41d4-a716-446655440000.mp4"
     *
     * 왜 원본 파일명과 저장 파일명을 분리할까?
     * - 같은 이름의 파일이 업로드되면 덮어쓰기 문제 발생
     * - 파일명에 한글, 특수문자가 있으면 문제가 될 수 있음
     * - 보안상 원본 파일명을 숨기는 것이 좋음
     */
    @Column(nullable = false)
    private String storedFileName;

    /**
     * 파일의 MIME 타입 (Content-Type)
     *
     * MIME 타입이란?
     * - 파일의 종류를 나타내는 표준 형식입니다
     * - 브라우저가 파일을 어떻게 처리할지 결정하는 데 사용됩니다
     *
     * 예시:
     * - "video/mp4" → MP4 동영상
     * - "video/webm" → WebM 동영상
     * - "image/png" → PNG 이미지
     * - "application/json" → JSON 데이터
     */
    @Column(nullable = false)
    private String contentType;

    /**
     * 파일 크기 (바이트 단위)
     *
     * 예: 104857600 = 100MB (100 * 1024 * 1024 bytes)
     *
     * Long 타입을 사용하는 이유:
     * - int의 최대값은 약 2GB (2,147,483,647 bytes)
     * - 대용량 동영상은 2GB를 넘을 수 있으므로 Long 사용
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * 업로드 일시
     *
     * LocalDateTime: Java 8부터 제공하는 날짜/시간 클래스
     * - 날짜와 시간 정보를 모두 포함합니다
     * - 예: 2024-01-15T14:30:00
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Video 객체를 생성하는 빌더 패턴 생성자
     *
     * @Builder 어노테이션 (Lombok):
     * - 빌더 패턴을 자동으로 생성해줍니다
     * - 빌더 패턴은 객체 생성 시 어떤 값을 어떤 필드에 넣는지 명확하게 보여줍니다
     *
     * 일반 생성자 vs 빌더 패턴:
     *
     * [일반 생성자 - 가독성 나쁨]
     * new Video("제목", "설명", "file.mp4", "uuid.mp4", "video/mp4", 1000L);
     * → 어떤 값이 어떤 필드인지 알기 어려움
     *
     * [빌더 패턴 - 가독성 좋음]
     * Video.builder()
     *     .title("제목")
     *     .description("설명")
     *     .fileName("file.mp4")
     *     .storedFileName("uuid.mp4")
     *     .contentType("video/mp4")
     *     .fileSize(1000L)
     *     .build();
     * → 각 값이 어떤 필드에 들어가는지 명확함
     */
    @Builder
    public Video(String title, String description, String fileName,
                 String storedFileName, String contentType, Long fileSize) {
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.storedFileName = storedFileName;
        this.contentType = contentType;
        this.fileSize = fileSize;
        // 객체 생성 시점의 현재 시간을 자동으로 설정합니다
        this.createdAt = LocalDateTime.now();
    }
}
