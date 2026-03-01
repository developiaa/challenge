package pro.developia._2026_03.domain.board;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {
    // 매핑 테이블에서 가져온 ID 목록으로 본 테이블 조회
    List<Board> findByBoardIdIn(List<Long> boardIds);
}
