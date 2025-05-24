package com.example.steps;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import static org.junit.Assert.assertEquals;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class StepDefinitions {

    private String apiEndpoint;
    private String param;
    private String method;
    private int actualStatusCode;
    private int expectedStatusCode;
    private final CloseableHttpClient httpClient;

    public StepDefinitions() {
        this.httpClient = HttpClients.createDefault();
    }

    @Given("I have an endpoint {string}")
    public void i_have_an_endpoint(String endpoint) {
        this.apiEndpoint = endpoint;
        System.out.println("Endpoint 설정: " + apiEndpoint);
    }

    @When("I call the API with param {string} using {string}")
    public void i_call_the_api_with_param_using_method(String param, String method) {
        this.param = param;
        this.method = method;
        System.out.println("Param 설정: " + this.param);
        System.out.println("Method 설정: " + this.method);
        
        try {
            HttpRequestBase request;
            switch (this.method.toUpperCase()) {
                case "POST":
                    HttpPost postRequest = new HttpPost(apiEndpoint);
                    postRequest.setEntity(new StringEntity(this.param));
                    request = postRequest;
                    break;
                case "PUT":
                    HttpPut putRequest = new HttpPut(apiEndpoint);
                    putRequest.setEntity(new StringEntity(this.param));
                    request = putRequest;
                    break;
                case "DELETE":
                    request = new HttpDelete(apiEndpoint);
                    break;
                case "GET":
                default:
                    String encodedParam = URLEncoder.encode(this.param, StandardCharsets.UTF_8.toString());
                    String delimiter = apiEndpoint.contains("?") ? "&" : "?";
                    String getUrl = apiEndpoint + delimiter + "param=" + encodedParam;
                    request = new HttpGet(getUrl);
                    break;
            }
            request.setHeader("Content-Type", "application/json");

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                this.actualStatusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                System.out.println("API 응답 상태 코드: " + actualStatusCode);
                System.out.println("API 응답 본문: " + responseBody);
            }
        } catch (Exception e) {
            System.err.println("API 호출 중 오류 발생: " + e.getMessage());
            this.actualStatusCode = 500;
        }
    }

    @Then("I get status code {int}")
    public void i_get_status_code(int statusCode) {
        this.expectedStatusCode = statusCode;
        System.out.println("예상 상태 코드: " + expectedStatusCode + " / 실제 상태 코드: " + actualStatusCode);
        assertEquals("Status code가 예상과 다릅니다.", expectedStatusCode, actualStatusCode);
    }
}

