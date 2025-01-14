package com.nooblol.board.dto;

import com.nooblol.board.utils.ArticleMessage;
import com.nooblol.board.utils.CategoryStatus;
import java.time.LocalDateTime;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.ObjectUtils;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CategoryUpdateDto extends CategoryRequestDto {

    @NotNull(message = ArticleMessage.CATEGORY_ID_NULL)
    private Integer categoryId;

    private String newCategoryName;
    private CategoryStatus status;

    @AssertTrue(message = ArticleMessage.CATEGORY_UPDATE_VALIDATION)
    public boolean isNewCategoryInfoValid() {
        if (StringUtils.isBlank(newCategoryName) && ObjectUtils.isEmpty(status)) {
            return false;
        }
        return true;
    }

    @Builder
    public CategoryUpdateDto(
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String createdUserId,
            String updatedUserId,
            Integer categoryId,
            String newCategoryName,
            CategoryStatus status) {
        super(createdAt, updatedAt, createdUserId, updatedUserId);
        this.categoryId = categoryId;
        this.newCategoryName = newCategoryName;
        this.status = status;
    }
}
