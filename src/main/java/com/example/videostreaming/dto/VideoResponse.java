package com.example.videostreaming.dto;

import com.example.videostreaming.entity.Video;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * VideoResponse - API 응답용 데이터 전송 객체 (DTO)
 * =============================================================================
 *
 * DTO (Data Transfer Object)란?
 * - 계층 간 데이터 전송을 위한 객체입니다
 * - 주로 Controller와 클라이언트(브라우저, 앱) 사이에서 데이터를 주고받을 때 사용합니다
 *
 * 왜 Entity를 직접 반환하지 않고 DTO를 사용할까?
 *
 * 1. 보안: Entity에는 노출하면 안 되는 정보가 있을 수 있습니다
 *    예: 비밀번호, 내부 ID 등
 *
 * 2. 유연성: 클라이언트에게 필요한 정보만 선택적으로 전달할 수 있습니다
 *    예: Entity에는 10개 필드가 있지만, 목록 조회 시에는 3개만 필요
 *
 * 3. 변경 분리: Entity가 변경되어도 API 응답 형식은 유지할 수 있습니다
 *    예: DB 컬럼명이 바뀌어도 API 응답의 필드명은 그대로 유지
 *
 * 4. 순환 참조 방지: Entity 간의 관계(양방향 참조)가 있을 때
 *    JSON 변환 시 무한 루프에 빠지는 것을 방지합니다
 *
 * 데이터 흐름:
 * [DB] → [Entity] → [DTO] → [JSON] → [클라이언트]
 */
@Getter  // Lombok: 모든 필드의 getter 메서드를 자동 생성합니다
public class VideoResponse {

    /**
     * 영상 ID
     */
    private Long id;

    /**
     * 영상 제목
     */
    private String title;

    /**
     * 영상 설명
     */
    private String description;

    /**
     * 원본 파일명
     */
    private String fileName;

    /**
     * 파일 크기 (바이트)
     */
    private Long fileSize;

    /**
     * 파일 MIME 타입 (예: video/mp4)
     */
    private String contentType;

    /**
     * 업로드 일시
     */
    private LocalDateTime createdAt;

    /**
     * Entity를 DTO로 변환하는 생성자
     *
     * 이렇게 Entity를 받아서 DTO로 변환하는 패턴을 자주 사용합니다.
     * Service에서 Entity를 조회한 후, Controller에 반환하기 전에
     * DTO로 변환합니다.
     *
     * 사용 예시:
     * Video video = videoRepository.findById(1L);
     * VideoResponse response = new VideoResponse(video);  // Entity → DTO 변환
     * return response;  // 클라이언트에게 반환
     *
     * @param video 변환할 Video 엔티티
     */
    public VideoResponse(Video video) {
        this.id = video.getId();
        this.title = video.getTitle();
        this.description = video.getDescription();
        this.fileName = video.getFileName();
        this.fileSize = video.getFileSize();
        this.contentType = video.getContentType();
        this.createdAt = video.getCreatedAt();

        // 참고: Entity의 storedFileName은 노출하지 않습니다!
        // 서버 내부에서만 사용하는 정보이기 때문입니다.
        // 이것이 DTO를 사용하는 이유 중 하나입니다.
    }
}
