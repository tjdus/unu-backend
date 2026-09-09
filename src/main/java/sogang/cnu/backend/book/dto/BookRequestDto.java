package sogang.cnu.backend.book.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BookRequestDto {
    @NotBlank(message = "도서명을 입력해주세요.")
    @Size(max = 200, message = "도서명은 200자 이내로 입력해주세요.")
    private String title;

    @NotBlank(message = "저자를 입력해주세요.")
    @Size(max = 120, message = "저자는 120자 이내로 입력해주세요.")
    private String author;

    @Size(max = 120, message = "출판사는 120자 이내로 입력해주세요.")
    private String publisher;

    @Size(max = 2000, message = "설명은 2000자 이내로 입력해주세요.")
    private String description;

    @NotNull(message = "보유 권수를 입력해주세요.")
    @Min(value = 1, message = "보유 권수는 1권 이상이어야 합니다.")
    @Max(value = 999, message = "보유 권수는 999권 이하여야 합니다.")
    private Integer quantity;

    @Size(max = 1000, message = "비고는 1000자 이내로 입력해주세요.")
    private String note;
}
