package pro.developia._2026_03.domain.board;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @Column(name = "board_id")
    private Long boardId; // 샤딩 키

    @Column(name = "user_id")
    private Long userId; // 헤비 유저의 ID

    private String title;
    private String content;
}
