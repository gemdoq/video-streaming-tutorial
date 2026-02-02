package com.example.videostreaming.service;

import com.example.videostreaming.dto.VideoResponse;
import com.example.videostreaming.entity.Video;
import com.example.videostreaming.exception.CustomException;
import com.example.videostreaming.repository.VideoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * VideoService - 영상 관련 비즈니스 로직을 처리하는 서비스
 * =============================================================================
 *
 * 서비스(Service) 계층의 역할:
 * - 비즈니스 로직을 처리합니다
 * - Controller와 Repository 사이에서 중간 역할을 합니다
 * - 트랜잭션을 관리합니다
 *
 * 계층 구조:
 * [Controller] → [Service] → [Repository] → [Database]
 *     요청 받음      로직 처리     DB 접근       데이터 저장
 *
 * @Service: 이 클래스가 서비스 계층의 Bean임을 나타냅니다
 *
 * @Transactional(readOnly = true):
 * - 이 클래스의 모든 메서드에 트랜잭션을 적용합니다
 * - readOnly = true: 읽기 전용 트랜잭션 (조회 성능 최적화)
 * - 데이터를 변경하는 메서드에는 별도로 @Transactional을 붙여 덮어씁니다
 *
 * 트랜잭션(Transaction)이란?
 * - 데이터베이스 작업의 단위입니다
 * - "모두 성공하거나 모두 실패하거나" (All or Nothing)
 * - 예: 송금 시 출금과 입금이 둘 다 성공해야 함
 *   출금만 되고 입금이 안 되면 문제가 생김
 */
@Service
@Transactional(readOnly = true)
public class VideoService {

    // =========================================================================
    // 의존성 주입 (Dependency Injection)
    // =========================================================================
    /**
     * VideoRepository: 데이터베이스 접근을 담당
     * FileStorageService: 파일 저장/조회를 담당
     *
     * final 키워드: 한 번 할당되면 변경 불가 (불변성 보장)
     *
     * 의존성 주입이란?
     * - 객체가 필요로 하는 다른 객체(의존성)를 외부에서 넣어주는 것
     * - new VideoRepository() 처럼 직접 생성하지 않고, Spring이 대신 넣어줍니다
     * - 이렇게 하면 테스트하기 쉽고, 코드 변경에 유연합니다
     */
    private final VideoRepository videoRepository;
    private final FileStorageService fileStorageService;

    /**
     * 생성자를 통한 의존성 주입 (Constructor Injection)
     *
     * Spring은 이 생성자를 보고 자동으로:
     * 1. VideoRepository Bean을 찾아서 첫 번째 파라미터에 전달
     * 2. FileStorageService Bean을 찾아서 두 번째 파라미터에 전달
     *
     * 생성자가 하나뿐이면 @Autowired를 생략해도 됩니다.
     *
     * @param videoRepository    영상 저장소 (Spring이 자동 주입)
     * @param fileStorageService 파일 저장 서비스 (Spring이 자동 주입)
     */
    public VideoService(VideoRepository videoRepository, FileStorageService fileStorageService) {
        this.videoRepository = videoRepository;
        this.fileStorageService = fileStorageService;
    }

    // =========================================================================
    // 영상 업로드
    // =========================================================================
    /**
     * 영상을 업로드합니다.
     *
     * @Transactional: 이 메서드는 데이터를 변경하므로 쓰기 트랜잭션을 사용합니다
     *                 (클래스 레벨의 readOnly=true를 덮어씁니다)
     *
     * 처리 과정:
     * 1. 파일을 서버에 저장 (FileStorageService)
     * 2. 영상 정보를 DB에 저장 (VideoRepository)
     * 3. 저장된 정보를 DTO로 변환하여 반환
     *
     * @param file        업로드된 파일
     * @param title       영상 제목
     * @param description 영상 설명 (선택)
     * @return 저장된 영상 정보 (VideoResponse DTO)
     */
    @Transactional
    public VideoResponse uploadVideo(MultipartFile file, String title, String description) {
        // 1. 파일을 서버의 파일 시스템에 저장하고, 저장된 파일명을 받습니다
        String storedFileName = fileStorageService.storeFile(file);

        // 2. Video 엔티티를 생성합니다 (빌더 패턴 사용)
        Video video = Video.builder()
                .title(title)                             // 영상 제목
                .description(description)                  // 영상 설명
                .fileName(file.getOriginalFilename())     // 원본 파일명
                .storedFileName(storedFileName)            // 저장된 파일명 (UUID)
                .contentType(file.getContentType())        // MIME 타입
                .fileSize(file.getSize())                  // 파일 크기 (바이트)
                .build();

        // 3. DB에 저장합니다
        // save() 메서드는 저장 후 ID가 채워진 엔티티를 반환합니다
        videoRepository.save(video);

        // 4. Entity를 DTO로 변환하여 반환합니다
        return new VideoResponse(video);
    }

    // =========================================================================
    // 영상 목록 조회
    // =========================================================================
    /**
     * 모든 영상 목록을 조회합니다. (최신순 정렬)
     *
     * @return 영상 목록 (VideoResponse DTO 리스트)
     */
    public List<VideoResponse> getAllVideos() {
        // 1. DB에서 영상 목록을 조회합니다 (Entity 리스트)
        // 2. 각 Entity를 DTO로 변환합니다
        // 3. 결과를 List로 수집하여 반환합니다

        return videoRepository.findAllByOrderByCreatedAtDesc()  // List<Video> 반환
                .stream()                        // Stream으로 변환 (연속 처리를 위해)
                .map(VideoResponse::new)         // 각 Video를 VideoResponse로 변환
                // 위 코드는 .map(video -> new VideoResponse(video)) 와 같습니다
                .collect(Collectors.toList());   // 다시 List로 수집
    }

    // =========================================================================
    // 영상 상세 조회
    // =========================================================================
    /**
     * 특정 영상의 상세 정보를 조회합니다.
     *
     * @param id 영상 ID
     * @return 영상 정보 (VideoResponse DTO)
     * @throws CustomException 영상을 찾을 수 없는 경우 404 에러
     */
    public VideoResponse getVideo(Long id) {
        // findById(): Optional<Video>를 반환합니다
        // orElseThrow(): 값이 있으면 반환, 없으면 예외를 던집니다
        Video video = videoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Video not found", HttpStatus.NOT_FOUND));

        return new VideoResponse(video);
    }

    /**
     * 영상 엔티티를 직접 조회합니다.
     *
     * 주로 내부적으로 사용합니다.
     * (스트리밍 시 파일 경로를 알아내기 위해)
     *
     * @param id 영상 ID
     * @return Video 엔티티
     * @throws CustomException 영상을 찾을 수 없는 경우 404 에러
     */
    public Video getVideoEntity(Long id) {
        return videoRepository.findById(id)
                .orElseThrow(() -> new CustomException("Video not found", HttpStatus.NOT_FOUND));
    }

    // =========================================================================
    // 영상 파일 경로 조회
    // =========================================================================
    /**
     * 영상 파일의 경로를 조회합니다.
     *
     * 스트리밍 시 파일을 읽기 위해 필요합니다.
     *
     * @param id 영상 ID
     * @return 파일 경로 (Path 객체)
     */
    public Path getVideoPath(Long id) {
        // 1. DB에서 영상 정보를 조회합니다
        Video video = getVideoEntity(id);

        // 2. 저장된 파일명으로 파일 경로를 가져옵니다
        return fileStorageService.getFilePath(video.getStoredFileName());
    }
}
