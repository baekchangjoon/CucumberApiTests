package com.example.cucumber;

import java.util.List;
import java.util.concurrent.*;

public class ParallelCucumberRunner {
    public static void main(String[] args) throws InterruptedException {
        // 1) 사용할 feature 목록 수집
        List<String> features = FeatureList.getAllFeatures(); // 총 5개라고 가정

        // 2) 스레드 풀 생성 (4개 스레드)
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 3) feature마다 작업(Callable/Runnable)을 만들어 제출
        for (String featureFile : features) {
            executor.submit(() -> {
                String endpoint = null;
                try {
                    // a) 가용한 서비스 획득 (없으면 대기)
                    endpoint = ServicePool.acquireService();

                    // b) 실제 Cucumber 테스트 실행 로직
                    //    "endpoint"를 어떻게 전달하느냐가 핵심.
                    runCucumberForFeature(featureFile, endpoint);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    // c) 사용 완료 → 서비스 풀에 되돌림
                    if (endpoint != null) {
                        try {
                            ServicePool.releaseService(endpoint);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        // 4) 모든 작업 완료까지 대기 & 종료
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        System.out.println("All feature tests completed.");
    }

    /**
     * 특정 feature 파일을 Cucumber로 실행하는 예시 메서드
     * endpoint는 환경 변수나 system property 등으로 전달해서
     * StepDefinitions 쪽에서 참조하도록 구성 가능
     */
    private static void runCucumberForFeature(String featureFile, String endpoint) throws Exception {
        // 예시 1) 자바 프로세스 내부에서 Cucumber API 직접 호출
        //    io.cucumber.core.cli.Main.run(...)
        // 예시 2) 별도 프로세스를 런칭하여 커맨드라인 cucumber 실행
        //    ProcessBuilder로 "cucumber --plugin pretty --feature {featureFile}" 호출 + ENV 설정
        // 아래는 "API 직접 호출" 예시 스케치:

        System.out.println("Running feature: " + featureFile + " on service: " + endpoint);

        // (1) 환경 변수/시스템 프로퍼티로 endpoint를 전달
        System.setProperty("TEST_SERVICE_ENDPOINT", endpoint);

        // (2) Cucumber 실행 파라미터 설정
        String[] argv = new String[] {
                "--threads", "1",        // 해당 feature는 단일 스레드
                "--plugin", "pretty",
                "--glue", "com.example.steps",
                featureFile
        };

        // (3) 실제 실행 (exit code를 받아볼 수 있음)
        byte exitStatus = io.cucumber.core.cli.Main.run(argv, Thread.currentThread().getContextClassLoader());
        if (exitStatus != 0) {
            throw new RuntimeException("Cucumber test failed for feature: " + featureFile);
        }
    }
}

