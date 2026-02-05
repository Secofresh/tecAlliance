package org.interview.tecalliance.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Discount information for articles with validity period")
public class Discount {

    @Schema(description = "Unique identifier of the discount", example = "1")
    private Long id;

    @Schema(description = "Description of the discount", example = "Summer Sale 2026")
    private String description;

    @Schema(description = "Discount percentage (0-100)", example = "15.00", minimum = "0", maximum = "100")
    private BigDecimal discountPercentage;

    @Schema(description = "Start date of discount validity (ISO-8601)", example = "2026-06-01")
    private LocalDate startDate;

    @Schema(description = "End date of discount validity (ISO-8601)", example = "2026-08-31")
    private LocalDate endDate;

    public boolean isValidOn(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }
}
