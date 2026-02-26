package pro.developia._2026_03.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 💡 User와 Article을 user_id 기준으로 한 번에 조회하는 로컬 JOIN 쿼리
    @Query("""
            SELECT u
            FROM User u
            JOIN FETCH u.articles
            WHERE u.userId = :userId""")
    Optional<User> findUserWithArticles(@Param("userId") Long userId);
}
