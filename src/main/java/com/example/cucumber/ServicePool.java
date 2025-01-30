package com.example.cucumber;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 서비스 엔드포인트 관리
 */
public class ServicePool {
    // 서비스(컨테이너) 4개: 포트가 다르다고 가정
    private static final List<String> SERVICE_ENDPOINTS = Arrays.asList(
            "http://localhost:28081",
            "http://localhost:28082",
            "http://localhost:28083",
            "http://localhost:28084"
    );

    // 사용 가능한 서비스 엔드포인트를 관리할 BlockingQueue
    private static final BlockingQueue<String> AVAILABLE_SERVICES = new LinkedBlockingQueue<>(SERVICE_ENDPOINTS);

    /**
     * 가용한 서비스를 하나 가져옴 (대기 상태일 수도 있음)
     */
    public static String acquireService() throws InterruptedException {
        return AVAILABLE_SERVICES.take();
    }

    /**
     * 사용 완료된 서비스를 다시 풀에 반환
     */
    public static void releaseService(String endpoint) throws InterruptedException {
        AVAILABLE_SERVICES.put(endpoint);
    }
}
