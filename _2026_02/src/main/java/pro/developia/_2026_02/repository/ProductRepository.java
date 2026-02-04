package pro.developia._2026_02.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.developia._2026_02.domain.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
}
