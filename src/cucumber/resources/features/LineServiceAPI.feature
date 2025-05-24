Feature: Line Service API Tests
  라인 서비스 관련 API를 테스트한다.

  Scenario Outline: 라인 서비스 API 테스트
    Given I have an endpoint <api_endpoint>
    When I call the API with param <param> using <method>
    Then I get status code <status_code>

    Examples:
      | testcase_id | method | api_endpoint                            | status_code | param                                                                 |
      | TID041      | "POST"   | "http://localhost:8082/api/lines"        | 200         | "{\"memberId\":1,\"lineNumber\":\"01012345678\",\"planName\":\"5G Basic\",\"status\":\"ACTIVE\"}" |
      | TID042      | "GET"    | "http://localhost:8082/api/lines"        | 200         | "{}"                                                                    |
      | TID043      | "GET"    | "http://localhost:8082/api/lines/142"    | 200         | "{}"                                                                    |
      | TID044      | "PUT"    | "http://localhost:8082/api/lines/142"    | 200         | "{\"memberId\":1,\"lineNumber\":\"01012345678\",\"planName\":\"5G Premium\",\"status\":\"SUSPENDED\"}" |
      | TID045      | "GET"    | "http://localhost:8082/api/lines/invalid"| 400         | "{}"                                                                    | 