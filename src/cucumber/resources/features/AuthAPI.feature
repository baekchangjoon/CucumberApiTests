Feature: Auth API Tests
  인증 관련 API를 테스트한다.

  Scenario Outline: 로그인/권한 확인 API 테스트
    Given I have an endpoint "<api_endpoint>"
    When I call the API with param "<param>"
    Then I get status code <status_code>

    Examples:
      | testcase_id | api_endpoint         | status_code | param      |
      | TID021      | /api/auth/login      | 200         | param1     |
      | TID022      | /api/auth/logout     | 200         | param2     |
      | TID023      | /api/auth/wrong      | 400         | param400   |
      | TID024      | /api/auth/user       | 200         | param3     |
      | TID025      | /api/auth/notfound   | 404         | param404   |
