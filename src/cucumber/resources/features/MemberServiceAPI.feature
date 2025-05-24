Feature: Member Service API Tests
  회원 서비스 관련 API를 테스트한다.

  Scenario Outline: 회원 서비스 API 테스트
    Given I have an endpoint <api_endpoint>
    When I call the API with param <param> using <method>
    Then I get status code <status_code>

    Examples:
      | testcase_id | method | api_endpoint                                | status_code | param                                                                 |
      | TID031      | "POST"   | "http://localhost:8081/api/members"          | 500         | "{\"memberName\":\"John Doe22\",\"email\":\"john@example.com\",\"age\":30,\"phoneNumber\":\"010-1234-5678\",\"isAdult\":true}"              |
      | TID032      | "GET"    | "http://localhost:8081/api/members/120"      | 200         | "{}"                                                                    |
      | TID033      | "GET"    | "http://localhost:8081/api/members"          | 200         | "{}"                                                                    |
      | TID034      | "PUT"    | "http://localhost:8081/api/members/120"      | 200         | "{\"memberName\":\"John Doe Updated\",\"email\":\"john.updated@example.com\",\"age\":31,\"phoneNumber\":\"010-9876-5432\",\"isAdult\":true}"|
      | TID035      | "GET"    | "http://localhost:8081/api/members/invalid"  | 400         | "{}"                                                                    |