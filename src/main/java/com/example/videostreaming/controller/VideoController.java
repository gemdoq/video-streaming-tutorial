package com.example.videostreaming.controller;

import com.example.videostreaming.dto.VideoResponse;
import com.example.videostreaming.entity.Video;
import com.example.videostreaming.service.VideoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * =============================================================================
 * VideoController - 영상 관련 REST API를 제공하는 컨트롤러
 * =============================================================================
 *
 * 컨트롤러(Controller)의 역할:
 * - 클라이언트(브라우저, 앱)의 HTTP 요청을 받습니다
 * - 적절한 서비스를 호출하여 비즈니스 로직을 처리합니다
 * - 처리 결과를 HTTP 응답으로 반환합니다
 *
 * REST API란?
 * - HTTP 프로토콜을 사용하여 데이터를 주고받는 방식입니다
 * - URL로 자원(Resource)을 표현하고, HTTP Method로 행위를 표현합니다
 *   - GET: 조회 (Read)
 *   - POST: 생성 (Create)
 *   - PUT: 수정 (Update)
 *   - DELETE: 삭제 (Delete)
 *
 * @RestController 어노테이션:
 * - @Controller + @ResponseBody를 합친 것입니다
 * - 이 클래스의 모든 메서드 반환값은 JSON으로 변환되어 응답 본문에 담깁니다
 * - 뷰(HTML 페이지)를 반환하지 않고 데이터를 직접 반환합니다
 *
 * @RequestMapping("/api/videos"):
 * - 이 컨트롤러의 모든 API 경로에 "/api/videos" 접두사가 붙습니다
 * - 예: /api/videos, /api/videos/1, /api/videos/1/stream
 */
@RestController
@RequestMapping("/api/videos")
public class VideoController {

    private final VideoService videoService;

    /**
     * 생성자 주입 (Constructor Injection)
     *
     * Spring이 자동으로 VideoService Bean을 찾아서 주입해줍니다.
     */
    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    // =========================================================================
    // 영상 업로드 API
    // =========================================================================
    /**
     * 영상을 업로드합니다.
     *
     * HTTP 요청 형식:
     * POST /api/videos
     * Content-Type: multipart/form-data
     *
     * form-data:
     * - file: 영상 파일
     * - title: 제목
     * - description: 설명 (선택)
     *
     * @PostMapping: HTTP POST 요청을 처리합니다
     *
     * @RequestParam 어노테이션:
     * - HTTP 요청의 파라미터를 메서드의 파라미터에 바인딩합니다
     * - "file": form-data에서 key가 "file"인 값
     * - required = false: 선택적 파라미터 (없어도 됨)
     *
     * MultipartFile:
     * - Spring에서 업로드된 파일을 다루는 인터페이스
     * - 파일 이름, 크기, 타입, 내용 등의 정보를 제공합니다
     *
     * ResponseEntity<T>:
     * - HTTP 응답 전체를 표현하는 클래스
     * - 응답 본문(body), 상태 코드(status), 헤더(headers)를 모두 설정할 수 있습니다
     *
     * @param file        업로드할 영상 파일
     * @param title       영상 제목
     * @param description 영상 설명 (선택)
     * @return 저장된 영상 정보와 201 Created 상태 코드
     */
    @PostMapping
    public ResponseEntity<VideoResponse> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) {

        // 서비스를 호출하여 영상을 업로드합니다
        VideoResponse response = videoService.uploadVideo(file, title, description);

        // 201 Created 상태 코드와 함께 응답을 반환합니다
        // status(HttpStatus.CREATED): 201 상태 코드 설정
        // body(response): 응답 본문에 VideoResponse를 담음 (자동으로 JSON 변환)
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // =========================================================================
    // 영상 목록 조회 API
    // =========================================================================
    /**
     * 모든 영상 목록을 조회합니다.
     *
     * HTTP 요청 형식:
     * GET /api/videos
     *
     * @GetMapping: HTTP GET 요청을 처리합니다
     *
     * @return 영상 목록과 200 OK 상태 코드
     */
    @GetMapping
    public ResponseEntity<List<VideoResponse>> getAllVideos() {
        List<VideoResponse> videos = videoService.getAllVideos();

        // ResponseEntity.ok(): 200 OK 상태 코드와 함께 응답을 반환합니다
        return ResponseEntity.ok(videos);
    }

    // =========================================================================
    // 영상 상세 조회 API
    // =========================================================================
    /**
     * 특정 영상의 상세 정보를 조회합니다.
     *
     * HTTP 요청 형식:
     * GET /api/videos/1
     * GET /api/videos/2
     *
     * @PathVariable 어노테이션:
     * - URL 경로의 일부를 변수로 받습니다
     * - /api/videos/{id} 에서 {id} 부분의 값을 파라미터로 전달받습니다
     * - 예: /api/videos/1 요청 시 id = 1
     *
     * @param id 조회할 영상의 ID
     * @return 영상 정보와 200 OK 상태 코드
     */
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> getVideo(@PathVariable Long id) {
        VideoResponse video = videoService.getVideo(id);
        return ResponseEntity.ok(video);
    }

    // =========================================================================
    // 영상 스트리밍 API (핵심!)
    // =========================================================================
    /**
     * 영상을 스트리밍합니다. (Range Request 지원)
     *
     * HTTP 요청 형식:
     * GET /api/videos/1/stream
     * Range: bytes=0-1048575
     *
     * ==========================================================================
     * Range Request란?
     * ==========================================================================
     *
     * 파일의 일부분만 요청하는 HTTP 기능입니다.
     * 영상 스트리밍에서 핵심적인 역할을 합니다.
     *
     * 왜 필요한가?
     * - 100MB 영상을 처음부터 끝까지 다 받을 때까지 기다리면 너무 오래 걸립니다
     * - Range Request를 사용하면 필요한 부분만 받아서 바로 재생할 수 있습니다
     *
     * 시크바(탐색바)를 드래그하면?
     * - 브라우저가 자동으로 해당 위치의 Range Request를 보냅니다
     * - 예: 50% 지점 클릭 → Range: bytes=52428800- 요청
     * - 서버가 그 위치부터 데이터를 보내줍니다
     *
     * ==========================================================================
     * HTTP 헤더 설명
     * ==========================================================================
     *
     * [요청 헤더]
     * Range: bytes=0-1048575
     * - "0번 바이트부터 1048575번 바이트까지 보내줘"
     *
     * Range: bytes=52428800-
     * - "52428800번 바이트부터 끝까지 보내줘"
     *
     * [응답 헤더]
     * HTTP/1.1 206 Partial Content
     * - "부분 내용을 보내줄게"
     *
     * Content-Range: bytes 0-1048575/104857600
     * - "전체 104857600 바이트 중 0~1048575 부분을 보내줄게"
     *
     * Accept-Ranges: bytes
     * - "나는 Range Request를 지원해"
     *
     * ==========================================================================
     *
     * @RequestHeader 어노테이션:
     * - HTTP 요청 헤더의 값을 파라미터로 받습니다
     * - value = HttpHeaders.RANGE: "Range" 헤더의 값을 받습니다
     * - required = false: Range 헤더가 없어도 됨 (일반 다운로드)
     *
     * @param id          영상 ID
     * @param rangeHeader Range 헤더 값 (예: "bytes=0-1048575")
     * @return 영상 데이터 (전체 또는 부분)
     * @throws IOException 파일 읽기 실패 시
     */
    @GetMapping("/{id}/stream")
    public ResponseEntity<byte[]> streamVideo(
            @PathVariable Long id,
            @RequestHeader(value = HttpHeaders.RANGE, required = false) String rangeHeader)
            throws IOException {

        // =====================================================================
        // 1. 영상 정보와 파일 경로 조회
        // =====================================================================
        Video video = videoService.getVideoEntity(id);
        Path videoPath = videoService.getVideoPath(id);

        // 파일 크기 조회 (바이트 단위)
        long fileSize = Files.size(videoPath);

        // =====================================================================
        // 2. Range 헤더가 없는 경우 - 전체 파일 반환
        // =====================================================================
        if (rangeHeader == null) {
            // 파일 전체를 읽어서 반환합니다
            // 주의: 대용량 파일에서는 메모리 문제가 발생할 수 있습니다
            byte[] data = Files.readAllBytes(videoPath);

            return ResponseEntity.ok()
                    // Content-Type: 파일의 MIME 타입 (예: video/mp4)
                    // 브라우저가 이 정보를 보고 비디오 플레이어를 사용합니다
                    .contentType(MediaType.parseMediaType(video.getContentType()))

                    // Content-Length: 응답 본문의 크기 (바이트)
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileSize))

                    // Accept-Ranges: 서버가 Range Request를 지원함을 알립니다
                    .header(HttpHeaders.ACCEPT_RANGES, "bytes")

                    // 응답 본문에 파일 데이터를 담습니다
                    .body(data);
        }

        // =====================================================================
        // 3. Range 헤더가 있는 경우 - 부분 파일 반환 (핵심!)
        // =====================================================================

        // -----------------------------------------------------------------
        // 3-1. Range 헤더 파싱
        // -----------------------------------------------------------------
        // Range 헤더 형식: "bytes=시작-끝" 또는 "bytes=시작-"
        // 예: "bytes=0-1048575" → 0번부터 1048575번 바이트까지
        // 예: "bytes=52428800-" → 52428800번부터 끝까지

        // "bytes=" 부분을 제거하고 "-"로 분리합니다
        String[] ranges = rangeHeader.replace("bytes=", "").split("-");

        // 시작 위치 (항상 존재)
        long rangeStart = Long.parseLong(ranges[0]);

        // 끝 위치
        // - 명시되어 있으면 그 값을 사용
        // - 없으면 시작 위치 + 1MB - 1 (또는 파일 끝)
        long rangeEnd;
        if (ranges.length > 1 && !ranges[1].isEmpty()) {
            // 끝 위치가 명시된 경우
            rangeEnd = Long.parseLong(ranges[1]);
        } else {
            // 끝 위치가 없는 경우: 1MB 청크 단위로 전송
            // 1024 * 1024 = 1MB (1,048,576 bytes)
            // -1을 하는 이유: 범위가 0부터 시작하므로
            rangeEnd = Math.min(rangeStart + 1024 * 1024 - 1, fileSize - 1);
        }

        // 끝 위치가 파일 크기를 초과하지 않도록 보정합니다
        if (rangeEnd >= fileSize) {
            rangeEnd = fileSize - 1;
        }

        // -----------------------------------------------------------------
        // 3-2. 해당 범위의 데이터 읽기
        // -----------------------------------------------------------------

        // 전송할 데이터의 크기
        long contentLength = rangeEnd - rangeStart + 1;

        // 데이터를 담을 바이트 배열 생성
        byte[] data = new byte[(int) contentLength];

        // RandomAccessFile: 파일의 임의 위치에 접근할 수 있는 클래스
        // - 일반 FileInputStream은 순차적으로만 읽을 수 있습니다
        // - RandomAccessFile은 seek()로 원하는 위치로 바로 이동할 수 있습니다
        // - 영상 스트리밍에서 필수적인 기능입니다!
        //
        // "r" = Read only (읽기 전용 모드)
        //
        // try-with-resources 문법:
        // - try 블록이 끝나면 자동으로 file.close()를 호출합니다
        // - 리소스 누수를 방지합니다
        try (RandomAccessFile file = new RandomAccessFile(videoPath.toFile(), "r")) {
            // seek(): 파일 포인터를 지정한 위치로 이동합니다
            // - 파일의 처음부터 순차적으로 읽지 않아도 됩니다
            // - 시크바를 드래그해서 중간부터 재생할 때 이 기능이 사용됩니다
            file.seek(rangeStart);

            // readFully(): 지정한 바이트 수만큼 정확히 읽습니다
            // - read()는 요청한 것보다 적게 읽을 수 있지만
            // - readFully()는 정확히 그 크기만큼 읽습니다
            file.readFully(data);
        }

        // -----------------------------------------------------------------
        // 3-3. 206 Partial Content 응답 반환
        // -----------------------------------------------------------------
        // 206: 부분 내용을 성공적으로 반환함을 나타내는 상태 코드
        // (200 OK와 다릅니다!)

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                // Content-Type: 영상의 MIME 타입
                .contentType(MediaType.parseMediaType(video.getContentType()))

                // Content-Length: 이번 응답에서 보내는 데이터의 크기
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))

                // Content-Range: 전체 파일 중 어느 부분을 보내는지 명시
                // 형식: "bytes 시작-끝/전체크기"
                // 예: "bytes 0-1048575/104857600"
                //     → "전체 104857600 바이트 중 0~1048575 부분입니다"
                .header(HttpHeaders.CONTENT_RANGE,
                        "bytes " + rangeStart + "-" + rangeEnd + "/" + fileSize)

                // Accept-Ranges: Range Request 지원 여부
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")

                // 응답 본문: 요청된 범위의 영상 데이터
                .body(data);
    }
}
