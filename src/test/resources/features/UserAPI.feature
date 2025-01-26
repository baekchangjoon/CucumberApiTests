Feature: User API Tests
  다양한 유저 관련 API를 테스트한다.

  Scenario Outline: 유저 정보 조회 테스트
    Given I have an endpoint "<api_endpoint>"
    When I call the API with param "<param>"
    Then I get status code <status_code>

    Examples:
      | testcase_id | api_endpoint       | status_code | param      |
      | TID001      | /api/users/1      | 200         | param1     |
      | TID002      | /api/users/2      | 200         | param2     |
      | TID003      | /api/users/abc    | 400         | param400   |
      | TID004      | /api/users/3      | 200         | param3     |
      | TID005      | /api/users/99     | 404         | param404   |
