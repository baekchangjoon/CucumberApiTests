Feature: Order API Tests
  주문 관련 API를 테스트한다.

  Scenario Outline: 주문 상세 조회 테스트
    Given I have an endpoint "<api_endpoint>"
    When I call the API with param "<param>"
    Then I get status code <status_code>

    Examples:
      | testcase_id | api_endpoint       | status_code | param      |
      | TID011      | /api/orders/1001   | 200         | param1     |
      | TID012      | /api/orders/1002   | 200         | param2     |
      | TID013      | /api/orders/abc    | 400         | param400   |
      | TID014      | /api/orders/9999   | 200         | param3     |
      | TID015      | /api/orders/0      | 404         | param404   |
