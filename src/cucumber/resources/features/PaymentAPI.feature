Feature: Payment API Tests
  결제 관련 API를 테스트한다.

  Scenario Outline: 결제 요청 상태 코드 확인 테스트
    Given I have an endpoint "<api_endpoint>"
    When I call the API with param "<param>"
    Then I get status code <status_code>

    Examples:
      | testcase_id | api_endpoint            | status_code | param      |
      | TID016      | /api/payments/credit    | 200         | param1     |
      | TID017      | /api/payments/debit     | 200         | param2     |
      | TID018      | /api/payments/error     | 400         | param400   |
      | TID019      | /api/payments/bitcoin   | 200         | param3     |
      | TID020      | /api/payments/invalid   | 404         | param404   |
