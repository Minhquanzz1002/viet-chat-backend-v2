package vn.edu.iuh.dto;

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
    private String filename;
    private UploadType type;
}
