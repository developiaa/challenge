package pro.developia._2026_03.domain.user;

import jakarta.persistence.*;
import lombok.*;
import pro.developia._2026_03.domain.article.Article;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {
    @Id
    @Column(name = "user_id")
    private Long userId; // 공통 샤딩 키

    private String username;

    // 양방향 매핑 (유저가 쓴 글 목록)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Article> articles = new ArrayList<>();

    public void addArticle(Article article) {
        articles.add(article);
        article.setUser(this);
    }
}
