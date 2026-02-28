package pro.developia._2026_03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import pro.developia._2026_03.domain.board.Board;
import pro.developia._2026_03.domain.board.BoardRepository;

import java.util.stream.LongStream;

/**
 * 코드 실행 필요 : _2026_02/docker/initdb/schema.sql
 * 각 샤드마다 board_{0..2} 생성 필요
 */
@SpringBootTest
@ActiveProfiles("shardingsphere")
public class HotspotResolutionTest {
    @Autowired
    private BoardRepository boardRepository;

    @Test
    @DisplayName("한 유저가 글을 폭격해도 3개의 DB, 9개의 테이블로 완벽히 분산된다")
    void testTableAndDatabaseComplexSharding() {
        Long heavyUserId = 999L; // 활동량이 엄청난 헤비 유저

        System.out.println("\n=======================================================");
        System.out.println(" [핫스팟 분산 테스트] 헤비 유저(999)가 연속으로 글 작성");
        System.out.println("=======================================================");

        LongStream.rangeClosed(1, 9).forEach(i -> {
            Board board = Board.builder()
                    .boardId(i)
                    .userId(heavyUserId)
                    .title("도배 글 " + i)
                    .content("서버 터져라!")
                    .build();

            boardRepository.save(board);
        });

        boardRepository.flush();
    }
}
