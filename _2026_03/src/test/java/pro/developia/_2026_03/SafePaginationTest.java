package pro.developia._2026_03;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import pro.developia._2026_03.domain.board.Board;
import pro.developia._2026_03.domain.board.BoardRepository;
import pro.developia._2026_03.domain.board.BoardUserMapping;
import pro.developia._2026_03.domain.board.BoardUserMappingRepository;

import java.util.List;
import java.util.stream.LongStream;

@SpringBootTest
@ActiveProfiles("shardingsphere")
public class SafePaginationTest {
    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardUserMappingRepository mappingRepository;

    @Test
    @DisplayName("매핑 테이블을 활용한 OOM 없는 안전한 페이징 처리 테스트")
    void testMappingTablePagination() {
        Long targetUserId = 777L; // 테스트 타겟 유저

        // =======================================================
        // 1. Data Setup: 유저 777이 15개의 글을 작성합니다.
        // =======================================================
        LongStream.rangeClosed(10, 25).forEach(i -> {
            // 본 테이블 (board_id 기준으로 9개로 찢어짐)
            Board board = Board.builder()
                    .boardId(i)
                    .userId(targetUserId)
                    .title("매핑 테스트 글 " + i)
                    .content("안전한 페이징을 위하여!")
                    .build();
            boardRepository.save(board);

            // 매핑 테이블 (user_id 기준으로 ds_0~2 중 한 곳에 몰려서 저장됨)
            BoardUserMapping mapping = BoardUserMapping.builder()
                    .mappingId(i)
                    .userId(targetUserId)
                    .boardId(i)
                    .build();
            mappingRepository.save(mapping);
        });

        // DB에 강제로 반영하여 로그 분리
        boardRepository.flush();
        mappingRepository.flush();


        // =======================================================
        // 2. Action: 최신순 1페이지 (10개) 조회 로직
        // =======================================================
        System.out.println("\n\n=======================================================");
        System.out.println(" [STEP 1] 매핑 테이블에서 '이번 페이지'의 ID 10개만 먼저 가져옵니다.");
        System.out.println(" 예상: targetUserId(777)이 있는 단일 샤드로만 쿼리가 1번 날아감!");
        System.out.println("=======================================================");

        PageRequest pageRequest = PageRequest.of(0, 10); // 0번 페이지, 10개
        List<Long> pagedBoardIds = mappingRepository.findBoardIdsByUserIdWithPagination(targetUserId, pageRequest);

        System.out.println(" => 추출된 게시글 ID: " + pagedBoardIds);


        System.out.println("\n=======================================================");
        System.out.println(" [STEP 2] 추출된 10개의 ID로 본 테이블(boards)에서 데이터를 긁어옵니다.");
        System.out.println(" 예상: IN 절을 통해 여러 샤드로 쿼리가 분산되지만, 가져오는 데이터는 딱 10개뿐!");
        System.out.println("=======================================================");

        List<Board> finalBoards = boardRepository.findByBoardIdIn(pagedBoardIds);

        // =======================================================
        // 3. Verify
        // =======================================================
        System.out.println("\n[결과] OOM 없이 안전하게 가져온 게시글 수: " + finalBoards.size() + "개");
        finalBoards.forEach(b -> System.out.println("- " + b.getTitle()));
    }
}
