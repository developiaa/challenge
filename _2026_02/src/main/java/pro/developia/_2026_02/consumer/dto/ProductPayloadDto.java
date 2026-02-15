package pro.developia._2026_02.consumer.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductPayloadDto {
    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("seller_id")
    private Long sellerId;

    @JsonProperty("product_name")
    private String productName;

    private String category;

    @JsonProperty("sales_price")
    private int salesPrice;

    @JsonProperty("manufacturer")
    private String manufacturer;

    @JsonProperty("product_status")
    private String productStatus;

    @JsonProperty("sales_start_date")
    private String salesStartDate;

    @JsonProperty("sales_end_date")
    private String salesEndDate;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("stock_quantity")
    private int stockQuantity;
}
