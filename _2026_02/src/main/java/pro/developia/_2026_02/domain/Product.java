package pro.developia._2026_02.domain;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Product {
    @Id
    private String productId;
    private Long sellerId;

    private String category;
    private String productName;
    private LocalDate salesStartDate;
    private LocalDate salesEndDate;
    @Enumerated(EnumType.STRING)
    private ProductStatus productStatus;
    private String brand;
    private String manufacturer;

    private int salesPrice;
    private int stockQuantity;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder
    public Product(String productId, Long sellerId, String category, String productName,
                   LocalDate salesStartDate, LocalDate salesEndDate, ProductStatus productStatus,
                   String brand, String manufacturer, int salesPrice, int stockQuantity) {
        this.productId = productId;
        this.sellerId = sellerId;
        this.category = category;
        this.productName = productName;
        this.salesStartDate = salesStartDate;
        this.salesEndDate = salesEndDate;
        this.productStatus = productStatus;
        this.brand = brand;
        this.manufacturer = manufacturer;
        this.salesPrice = salesPrice;
        this.stockQuantity = stockQuantity;
    }
}

