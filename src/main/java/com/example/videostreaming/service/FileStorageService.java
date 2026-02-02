package com.example.videostreaming.service;

import com.example.videostreaming.exception.CustomException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

/**
 * =============================================================================
 * FileStorageService - 파일 저장 및 조회를 담당하는 서비스
 * =============================================================================
 *
 * 이 클래스의 역할:
 * - 업로드된 파일을 서버의 파일 시스템에 저장합니다
 * - 저장된 파일의 경로를 조회합니다
 * - 허용된 파일 형식인지 검증합니다
 *
 * @Service 어노테이션:
 * - 이 클래스가 서비스 계층의 컴포넌트임을 나타냅니다
 * - Spring이 자동으로 이 클래스의 객체(Bean)를 생성하고 관리합니다
 * - 다른 클래스에서 @Autowired나 생성자 주입으로 사용할 수 있습니다
 */
@Service
public class FileStorageService {

    /**
     * 파일이 저장될 디렉토리 경로
     *
     * @Value 어노테이션:
     * - application.yml의 설정값을 이 필드에 주입합니다
     * - "${file.upload-dir}": application.yml의 file.upload-dir 값을 가져옵니다
     *
     * application.yml에서 다음과 같이 설정되어 있습니다:
     * file:
     *   upload-dir: ${FILE_UPLOAD_DIR:./uploads}
     */
    @Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * 파일 저장 경로를 Path 객체로 저장
     *
     * Path: Java NIO의 파일 경로를 나타내는 클래스
     * - 운영체제에 독립적인 파일 경로 처리가 가능합니다
     * - Windows: C:/uploads/file.mp4
     * - Linux/Mac: /uploads/file.mp4
     */
    private Path uploadPath;

    /**
     * 허용되는 파일 MIME 타입 목록
     *
     * Set: 중복을 허용하지 않는 컬렉션
     * Set.of(): 불변(immutable) Set을 생성합니다
     *
     * 보안을 위해 허용된 파일 형식만 업로드할 수 있도록 제한합니다.
     * - video/mp4: MP4 동영상
     * - video/webm: WebM 동영상
     * - video/quicktime: MOV 동영상 (QuickTime)
     */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "video/mp4",
            "video/webm",
            "video/quicktime"
    );

    /**
     * 서비스 초기화 메서드
     *
     * @PostConstruct 어노테이션:
     * - Bean이 생성되고 의존성 주입이 완료된 후에 자동으로 실행됩니다
     * - 초기화 작업(폴더 생성 등)을 수행하기에 적합합니다
     *
     * 실행 순서:
     * 1. FileStorageService 객체 생성
     * 2. @Value로 uploadDir 값 주입
     * 3. @PostConstruct가 붙은 init() 메서드 실행
     */
    @PostConstruct
    public void init() {
        // Paths.get(): 문자열 경로를 Path 객체로 변환합니다
        // toAbsolutePath(): 상대 경로를 절대 경로로 변환합니다
        //   예: "./uploads" → "/Users/user/project/uploads"
        // normalize(): 경로에서 불필요한 부분을 제거합니다
        //   예: "/a/b/../c" → "/a/c"
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            // Files.createDirectories(): 경로에 해당하는 디렉토리를 생성합니다
            // - 중간 경로의 디렉토리도 함께 생성합니다 (mkdir -p와 같음)
            // - 이미 존재하면 아무 일도 하지 않습니다 (에러 없음)
            Files.createDirectories(uploadPath);

            System.out.println("Upload directory created: " + uploadPath);
        } catch (IOException e) {
            // 디렉토리 생성 실패 시 애플리케이션 시작을 중단합니다
            throw new RuntimeException("Could not create upload directory: " + uploadPath, e);
        }
    }

    /**
     * 파일을 저장하고 저장된 파일명을 반환합니다.
     *
     * @param file 업로드된 파일 (MultipartFile)
     *             - MultipartFile: Spring에서 업로드된 파일을 다루는 인터페이스
     *             - 파일 내용, 이름, 크기, 타입 등의 정보를 제공합니다
     *
     * @return 저장된 파일명 (UUID 기반)
     *
     * @throws CustomException 파일 형식이 유효하지 않거나 저장 실패 시
     */
    public String storeFile(MultipartFile file) {
        // =====================================================================
        // 1. 파일 형식 검증
        // =====================================================================
        String contentType = file.getContentType();

        // 허용된 파일 형식인지 확인합니다
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            // 허용되지 않은 파일 형식이면 400 Bad Request 에러를 던집니다
            throw new CustomException(
                    "Invalid file type. Allowed types: mp4, webm, mov",
                    HttpStatus.BAD_REQUEST
            );
        }

        // =====================================================================
        // 2. 저장할 파일명 생성 (UUID 사용)
        // =====================================================================
        // 원본 파일명에서 확장자를 추출합니다
        String originalFileName = file.getOriginalFilename();
        String extension = "";

        if (originalFileName != null && originalFileName.contains(".")) {
            // lastIndexOf("."): 마지막 점(.)의 위치를 찾습니다
            // substring(): 그 위치부터 끝까지 잘라냅니다
            // 예: "my_video.mp4" → ".mp4"
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }

        // UUID (Universally Unique Identifier): 전 세계적으로 고유한 식별자
        // - 128비트 숫자로 구성됩니다
        // - 중복될 확률이 거의 0에 가깝습니다
        // 예: "550e8400-e29b-41d4-a716-446655440000"
        String storedFileName = UUID.randomUUID().toString() + extension;

        // =====================================================================
        // 3. 파일 저장
        // =====================================================================
        try {
            // resolve(): uploadPath와 파일명을 합쳐서 전체 경로를 만듭니다
            // 예: "/uploads" + "uuid.mp4" → "/uploads/uuid.mp4"
            Path targetLocation = uploadPath.resolve(storedFileName);

            // Files.copy(): 파일을 복사합니다
            // - file.getInputStream(): 업로드된 파일의 내용을 읽는 스트림
            // - targetLocation: 저장할 위치
            // - REPLACE_EXISTING: 같은 이름의 파일이 있으면 덮어씁니다
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("File saved: " + targetLocation);

            // 저장된 파일명을 반환합니다 (DB에 저장할 용도)
            return storedFileName;

        } catch (IOException e) {
            // 파일 저장 실패 시 500 Internal Server Error
            throw new CustomException(
                    "Could not store file: " + originalFileName,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /**
     * 저장된 파일의 경로(Path)를 반환합니다.
     *
     * 이 메서드는 파일 스트리밍 시 파일의 위치를 찾기 위해 사용됩니다.
     *
     * @param storedFileName 저장된 파일명 (UUID 기반)
     * @return 파일의 전체 경로 (Path 객체)
     *
     * 예시:
     * getFilePath("550e8400-e29b-41d4-a716-446655440000.mp4")
     * → "/Users/user/project/uploads/550e8400-e29b-41d4-a716-446655440000.mp4"
     */
    public Path getFilePath(String storedFileName) {
        // resolve(): uploadPath와 파일명을 합칩니다
        // normalize(): 경로를 정규화합니다 (보안상 중요!)
        //   - "../" 같은 상위 디렉토리 접근 시도를 방지합니다
        //   - 예: "uploads/../etc/passwd" 같은 공격 방지
        return uploadPath.resolve(storedFileName).normalize();
    }
}
