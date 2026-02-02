package com.example.videostreaming;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * =============================================================================
 * VideoStreamingApplication - Spring Boot 애플리케이션의 시작점
 * =============================================================================
 *
 * 이 클래스의 역할:
 * - Spring Boot 애플리케이션을 시작하는 메인 클래스입니다
 * - main() 메서드가 있어서 Java 프로그램의 진입점 역할을 합니다
 *
 * @SpringBootApplication 어노테이션의 의미:
 * 이 어노테이션 하나가 아래 3개의 어노테이션을 합친 것입니다:
 *
 * 1. @SpringBootConfiguration
 *    - 이 클래스가 Spring의 설정 클래스임을 나타냅니다
 *
 * 2. @EnableAutoConfiguration
 *    - Spring Boot가 자동으로 필요한 설정들을 해줍니다
 *    - 예: 웹 서버 설정, 데이터베이스 연결 설정 등
 *
 * 3. @ComponentScan
 *    - 이 클래스가 있는 패키지(com.example.videostreaming)와
 *      그 하위 패키지에서 Spring 컴포넌트들을 자동으로 찾아 등록합니다
 *    - @Controller, @Service, @Repository, @Component 등이 붙은 클래스들
 */
@SpringBootApplication
public class VideoStreamingApplication {

    /**
     * 애플리케이션의 시작점 (Entry Point)
     *
     * Java 프로그램은 항상 main() 메서드에서 시작합니다.
     * Spring Boot는 이 main() 메서드에서 SpringApplication.run()을 호출하여
     * 애플리케이션을 시작합니다.
     *
     * @param args 커맨드 라인에서 전달받은 인자들
     *             예: java -jar app.jar --server.port=9090
     */
    public static void main(String[] args) {
        // SpringApplication.run() 메서드가 하는 일:
        // 1. Spring 애플리케이션 컨텍스트(Application Context)를 생성합니다
        //    - 애플리케이션 컨텍스트: Spring이 관리하는 모든 객체(Bean)들의 컨테이너
        // 2. 내장 웹 서버(Tomcat)를 시작합니다
        // 3. 컴포넌트 스캔을 통해 @Controller, @Service 등을 찾아 Bean으로 등록합니다
        // 4. 자동 설정(Auto Configuration)을 적용합니다
        SpringApplication.run(VideoStreamingApplication.class, args);
    }
}
