package com.example.videostreaming.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * =============================================================================
 * CustomException - 커스텀 예외 클래스
 * =============================================================================
 *
 * 이 클래스의 역할:
 * - 애플리케이션에서 발생하는 예외(에러)를 처리하기 위한 커스텀 예외 클래스입니다
 * - HTTP 상태 코드와 에러 메시지를 함께 담아서 클라이언트에게 전달합니다
 *
 * 왜 커스텀 예외가 필요한가?
 * - 기본 예외(Exception)만 사용하면 에러의 종류를 구분하기 어렵습니다
 * - HTTP 상태 코드(404, 400 등)를 함께 전달하고 싶을 때 유용합니다
 * - 예: "파일을 찾을 수 없음" → 404 Not Found
 * - 예: "잘못된 파일 형식" → 400 Bad Request
 *
 * 상속 구조:
 * RuntimeException을 상속받습니다.
 * - RuntimeException: 실행 중에 발생하는 예외 (Unchecked Exception)
 * - 컴파일 시점에 예외 처리(try-catch)를 강제하지 않습니다
 *
 * HTTP 상태 코드란?
 * - 서버가 클라이언트에게 요청 결과를 숫자로 알려주는 코드입니다
 * - 200: 성공 (OK)
 * - 201: 생성 성공 (Created)
 * - 400: 잘못된 요청 (Bad Request) - 클라이언트 실수
 * - 404: 찾을 수 없음 (Not Found)
 * - 500: 서버 내부 오류 (Internal Server Error) - 서버 실수
 */
@Getter  // Lombok: getStatus(), getMessage() 등 getter 메서드를 자동 생성합니다
public class CustomException extends RuntimeException {

    /**
     * HTTP 상태 코드를 저장하는 필드
     *
     * final 키워드: 한 번 설정되면 변경할 수 없음 (불변)
     * private: 외부에서 직접 접근 불가 (getter로만 접근)
     */
    private final HttpStatus status;

    /**
     * CustomException 생성자
     *
     * 예외를 생성할 때 에러 메시지와 HTTP 상태 코드를 함께 전달합니다.
     *
     * @param message 에러 메시지 (예: "Video not found")
     * @param status  HTTP 상태 코드 (예: HttpStatus.NOT_FOUND)
     *
     * 사용 예시:
     * throw new CustomException("Video not found", HttpStatus.NOT_FOUND);
     * throw new CustomException("Invalid file type", HttpStatus.BAD_REQUEST);
     */
    public CustomException(String message, HttpStatus status) {
        // super(message): 부모 클래스(RuntimeException)의 생성자를 호출합니다
        // RuntimeException은 message를 저장하고, getMessage()로 조회할 수 있게 해줍니다
        super(message);

        // HTTP 상태 코드를 저장합니다
        this.status = status;
    }
}
