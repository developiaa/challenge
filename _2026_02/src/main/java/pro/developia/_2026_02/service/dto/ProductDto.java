package pro.developia._2026_02.service.dto;

import lombok.*;
import pro.developia._2026_02.domain.Product;
import pro.developia._2026_02.domain.ProductStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ProductDto {
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

    public static ProductDto from(Product product) {
        return new ProductDto(product.getProductId(), product.getSellerId(), product.getCategory(),
                product.getProductName(), product.getSalesStartDate(),
                product.getSalesEndDate(), product.getProductStatus(),
                product.getBrand(), product.getManufacturer(), product.getSalesPrice(),
                product.getStockQuantity());
    }
}
