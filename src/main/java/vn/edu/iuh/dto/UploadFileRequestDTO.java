package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.edu.iuh.dto.enums.UploadType;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UploadFileRequestDTO {
    @NotNull(message = "Tên file là bắt buộc")
    @Pattern(regexp = "^[\\w\\s-_]+\\.[A-Za-z]+$", message = "File phải có dạng <file>.<extension> VD: example.txt ")
    private String filename;
    @NotNull(message = "Loại file là bắt buộc")
    private UploadType type;
}
