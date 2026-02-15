package pro.developia._2026_02.controller.request;


import lombok.Getter;
import lombok.Setter;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class CreateProductRequest {
    private String productId;
    private Long sellerId;
    private String category;
    private String productName;
    private LocalDate salesStartDate;
    private LocalDate salesEndDate;
    private ProductStatus productStatus;
    private String brand;
    private String manufacturer;
    private int salesPrice;
    private int stockQuantity;

    public Product toEntity() {
        return Product.builder()
                .productId(UUID.randomUUID().toString())
                .sellerId(sellerId)
                .category(category)
                .productName(productName)
                .salesStartDate(salesStartDate)
                .salesEndDate(salesEndDate)
                .productStatus(productStatus)
                .brand(brand)
                .manufacturer(manufacturer)
                .salesPrice(salesPrice)
                .stockQuantity(stockQuantity)
                .build();
    }
}
