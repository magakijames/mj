package com.snapandpack.dto;

import com.snapandpack.model.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public final class ProductDtos {

    private ProductDtos() {
    }

    public record ProductRequest(
            @NotBlank(message = "Enter a product name") @Size(max = 150) String name,
            @NotBlank @Pattern(regexp = "Bag|Cap", message = "Category must be Bag or Cap") String category,
            @NotBlank @Pattern(regexp = "Men|Women|Unisex|Kids", message = "Choose Men, Women, Unisex or Kids") String gender,
            @NotEmpty(message = "Add at least one colour") List<@NotBlank @Size(max = 50) String> colors,
            @NotEmpty(message = "Add at least one size") List<@NotBlank @Size(max = 50) String> sizes,
            @NotNull(message = "Enter a price") @DecimalMin(value = "0", message = "Price cannot be negative") BigDecimal price,
            @Min(value = 0, message = "Stock cannot be negative") int stock,
            @Size(max = 500) String image) {
    }

    public record ProductResponse(Long id, String name, String category, String gender, List<String> colors,
                                  List<String> sizes, BigDecimal price, int stock, String image) {

        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getCategory(), p.getGender(),
                    List.copyOf(p.getColors()), List.copyOf(p.getSizes()), p.getPrice(), p.getStock(), p.getImage());
        }
    }
}
