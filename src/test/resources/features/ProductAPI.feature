Feature: Product API Tests
  상품 관련 API를 테스트한다.

  Scenario Outline: 상품 정보 조회 테스트
    Given I have an endpoint "<api_endpoint>"
    When I call the API with param "<param>"
    Then I get status code <status_code>

    Examples:
      | testcase_id | api_endpoint           | status_code | param      |
      | TID006      | /api/products/10       | 200         | param1     |
      | TID007      | /api/products/50       | 200         | param2     |
      | TID008      | /api/products/xyz      | 400         | param400   |
      | TID009      | /api/products/101      | 200         | param3     |
      | TID010      | /api/products/notfound | 404         | param404   |
