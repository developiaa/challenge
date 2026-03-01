package pro.developia._2026_03.domain.board;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "board_user_mappings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BoardUserMapping {
    @Id
    @Column(name = "mapping_id")
    private Long mappingId;

    @Column(name = "user_id")
    private Long userId; // 샤딩 키

    @Column(name = "board_id")
    private Long boardId;
}
