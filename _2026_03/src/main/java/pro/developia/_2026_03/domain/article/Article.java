package pro.developia._2026_03.domain.article;

import jakarta.persistence.*;
import lombok.*;
import pro.developia._2026_03.domain.user.User;

@Entity
@Table(name = "articles")
@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Article {
    @Id
    @Column(name = "article_id")
    private Long articleId;

    // 유저와의 연관관계 (이 user_id가 샤딩 키로 동작합니다)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String title;
    private String content;
}
