package pro.developia._2026_03.domain.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, String> {

    // ==========================================
    // 1. SELECT 테스트용
    // ==========================================
    // 샤딩 키(sellerId) 포함
    List<Product> findBySellerId(Long sellerId);

    // 샤딩 키 미포함 (카테고리로만 검색)
    List<Product> findByCategory(String category);

    // ==========================================
    // 2. UPDATE 테스트용 (벌크 연산)
    // ==========================================
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.salesPrice = :price WHERE p.sellerId = :sellerId")
    void updatePriceBySellerId(@Param("sellerId") Long sellerId, @Param("price") int price);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.salesPrice = :price WHERE p.category = :category")
    void updatePriceByCategory(@Param("category") String category, @Param("price") int price);

    // ==========================================
    // 3. DELETE 테스트용 (벌크 연산)
    // ==========================================
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.sellerId = :sellerId")
    void deleteBySellerId(@Param("sellerId") Long sellerId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.category = :category")
    void deleteByCategory(@Param("category") String category);
}
