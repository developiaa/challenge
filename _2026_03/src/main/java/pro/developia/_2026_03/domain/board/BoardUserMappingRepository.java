package pro.developia._2026_03.domain.board;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BoardUserMappingRepository extends JpaRepository<BoardUserMapping, Long> {

    // 매핑 테이블에서 "미리 페이징"을 수행하여 딱 10개의 boardId만 가져옴
    // Slice나 List를 반환타입으로 쓰면 무거운 Count 쿼리를 피할 수 있다.
    @Query("""
            SELECT m.boardId
            FROM BoardUserMapping m
            WHERE m.userId = :userId
            ORDER BY m.mappingId DESC""")
    List<Long> findBoardIdsByUserIdWithPagination(@Param("userId") Long userId, Pageable pageable);
}
