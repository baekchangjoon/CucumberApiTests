package com.example.runner;

import org.junit.runner.RunWith;
import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

// JUnit 4 기준 예시
@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",  // Feature 파일 경로
        glue = {"com.example.stepdefinitions"},    // StepDefinition 패키지 경로
        plugin = {"pretty", "json:target/cucumber.json", "html:target/Cucumber.html"},
        monochrome = true
)
public class CucumberTestRunner {
    // 별도 코드 없이 어노테이션 설정으로 실행
}
