package com.example.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;

import static org.junit.Assert.*;

public class StepDefinitions {

    private String apiEndpoint;
    private String param;
    private int actualStatusCode;
    private int expectedStatusCode;

    @Given("I have an endpoint {string}")
    public void i_have_an_endpoint(String endpoint) {
        this.apiEndpoint = endpoint;
        // 실제 테스트 시나리오에서는 여기서 API URL 설정 로직 등을 구성할 수 있음
        System.out.println("Endpoint 설정: " + apiEndpoint);
    }

    @When("I call the API with param {string}")
    public void i_call_the_api_with_param(String param) {
        this.param = param;
        // 실제 로직 예시: HTTP Client로 apiEndpoint에 요청 보내기
        // 요청 시 param을 쿼리 파라미터로 넘기는 등의 과정
        // 응답 값에서 status code를 추출
        // 여기서는 예시로만 status code를 임의로 설정
        System.out.println("Param 설정: " + param);

        // 실제 API 호출 예시 (의사 코드):
        // HttpResponse response = HttpClient.call(apiEndpoint, param);
        // this.actualStatusCode = response.getStatusCode();

        // 데모를 위해 임의 값 설정
        // 여기서는 param에 따라 결과 코드를 달리한다 가정
        if ("param400".equalsIgnoreCase(param)) {
            this.actualStatusCode = 400;
        } else if ("param404".equalsIgnoreCase(param)) {
            this.actualStatusCode = 404;
        } else {
            this.actualStatusCode = 200; // 기본 200
        }
    }

    @Then("I get status code {int}")
    public void i_get_status_code(int statusCode) {
        this.expectedStatusCode = statusCode;
        System.out.println("예상 상태 코드: " + expectedStatusCode + " / 실제 상태 코드: " + actualStatusCode);
        assertEquals("Status code가 예상과 다릅니다.", expectedStatusCode, actualStatusCode);
    }
}

