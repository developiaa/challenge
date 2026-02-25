# 시스템 아키텍처
- 2026-02 모듈에서 이어진 내용으로 데이터베이스는 기존 모듈을 참고할 것.
- shardingsphere, vitess 라이브러리를 이용한 샤딩을 구현한다.

# Test
- ShardingSphereTest
  - 기본적인 사용법 테스트
  - 샤딩키 유무에 따른 쿼리 횟수 검증
    - SELECT, UPDATE, DELETE
- JoinRoutingTest
  - 크로스 샤드 JOIN FETCH 테스트
