package pro.developia._2026_03;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;
import pro.developia._2026_03.domain.article.Article;
import pro.developia._2026_03.domain.article.ArticleRepository;
import pro.developia._2026_03.domain.user.User;
import pro.developia._2026_03.domain.user.UserRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("shardingsphere")
@ExtendWith(OutputCaptureExtension.class)
public class UserCentricColocationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ArticleRepository articleRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
        articleRepository.deleteAll();
    }

    @Test
    @DisplayName("다른 애그리거트 간의 user_id 기반 Data Co-location 로컬 조인 테스트")
    void testUserCentricJoin(CapturedOutput output) {
        // 1. Data Setup (user_id = 200 ➔ 200 % 3 = 2 ➔ ds_2 에 배정)
        Long userId = 200L;

        User user = User.builder()
                .userId(userId)
                .username("개발자 A")
                .build();

        // 게시글 추가 (이 게시글들은 모두 작성자인 '개발자 A'의 user_id를 따라 ds_2에 저장됨)
        user.addArticle(
                Article.builder()
                        .articleId(2001L)
                        .title("샤딩 완벽 정복")
                        .build()
        );
        user.addArticle(
                Article.builder()
                        .articleId(2002L)
                        .title("바인딩 테이블의 응용")
                        .build()
        );

        userRepository.save(user);
        userRepository.flush(); // DB에 Insert 쿼리 즉시 발생

        System.out.println("\n=======================================================");
        System.out.println(" [조인 쿼리 실행] user_id 를 기준으로 JOIN FETCH 실행");
        System.out.println("=======================================================");

        int preLength1 = output.getOut().length();

        // 2. Action: JOIN FETCH 쿼리 실행
        Optional<User> fetchedUser = userRepository.findUserWithArticles(userId);

        // 방금 실행한 액션 이후의 로그만 추출
        String logs1 = output.getOut().substring(preLength1);
        // "Actual SQL: ds_" 문자열이 몇 번 등장했는지 카운트
        int queryCount1 = logs1.split("Actual SQL: ds_").length - 1;

        // 3. Verify
        assertThat(queryCount1).isEqualTo(1);

        fetchedUser.ifPresent(u -> {
            System.out.println("조회된 유저 이름: " + u.getUsername());
            System.out.println("작성한 게시글 개수: " + u.getArticles().size() + "개");
        });
    }
}
