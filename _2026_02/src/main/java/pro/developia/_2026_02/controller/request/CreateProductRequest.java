package pro.developia._2026_02.controller.request;


import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;

import java.time.LocalDate;

public class CreateProductRequest {
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
