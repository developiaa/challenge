# 시스템 아키텍처
이 구조의 핵심은 쓰기(Command)는 MySQL 샤드에 집중하고, 조회(Query)는 고속 검색 엔진에서 수행하여 동기화 지연을 허용하는 최종 일관성(Eventual Consistency) 모델

# Debezium을 이용한 실시간 CDC(Change Data Capture) 구축 가이드
- Debezium은 MySQL의 Binlog를 읽어서 작동합니다. 현재 실행 중인 MySQL 컨테이너들이 Binlog를 ROW 포맷으로 기록하도록 설정해야 합니다.
- initdb/setup.sql 또는 각 DB에 접속하여 실행: CDC 전용 계정을 생성하고 권한을 부여합니다.



# docker 실행 후 샤드 등록
- mysql 컨테이너마다 등록해주어야함
- database.hostname, database.server.id, topic.prefix, schema.history.internal.kafka.topic 가 달라야함

1번 샤드
```http request
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{
  "name": "inventory-connector-0",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "mysql-3308", 
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.server.id": "5001",
    "topic.prefix": "shard0",
    "database.include.list": "products",
    "schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
    "schema.history.internal.kafka.topic": "schemahistory.shard0",
    "database.connectionTimeZone": "Asia/Seoul"
  }
}'
```


2번 샤드
```http request
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{
  "name": "inventory-connector-1",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "mysql-3309", 
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.server.id": "5002",
    "topic.prefix": "shard1",
    "database.include.list": "products",
    "schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
    "schema.history.internal.kafka.topic": "schemahistory.shard1",
    "database.connectionTimeZone": "Asia/Seoul"
  }
}'
```


3번 샤드
```http request
curl -i -X POST -H "Accept:application/json" -H "Content-Type:application/json" localhost:8083/connectors/ -d '{
  "name": "inventory-connector-2",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "tasks.max": "1",
    "database.hostname": "mysql-3310", 
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.server.id": "5003",
    "topic.prefix": "shard2",
    "database.include.list": "products",
    "schema.history.internal.kafka.bootstrap.servers": "kafka:29092",
    "schema.history.internal.kafka.topic": "schemahistory.shard2",
    "database.connectionTimeZone": "Asia/Seoul"
  }
}'
```

# 커넥터 상태 확인(3개 모두 RUNNING이어야 함)
```http request
curl -s "localhost:8083/connectors?expand=status"
```

# 기존 커넥터 삭제
```http request
curl -X DELETE localhost:8083/connectors/inventory-connector-0
curl -X DELETE localhost:8083/connectors/inventory-connector-1
curl -X DELETE localhost:8083/connectors/inventory-connector-2
```


# Kafka 토픽 생성 확인
```http request
docker exec -it docker-kafka-1 kafka-topics --list --bootstrap-server localhost:9092
```

