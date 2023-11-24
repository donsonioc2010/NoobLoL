package com.nooblol.board.dto;

import com.nooblol.board.utils.CategoryStatus;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDto {

    private int categoryId;
    private String categoryName;
    private CategoryStatus status;
    private String createdUserId;
    private LocalDateTime createdAt;
    private String updatedUserId;
    private LocalDateTime updatedAt;
}
