package com.example.cucumber;

import java.util.Arrays;
import java.util.List;

public class FeatureList {
    public static List<String> getAllFeatures() {
        // 실제로는 디렉토리 스캔, 파일 목록 수집 로직을 넣을 수 있음
        return Arrays.asList(
                "src/test/resources/features/featureA.feature",
                "src/test/resources/features/featureB.feature",
                "src/test/resources/features/featureC.feature",
                "src/test/resources/features/featureD.feature",
                "src/test/resources/features/featureE.feature"  // 예시로 5개
        );
    }
}

