package vn.edu.iuh.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UploadFileResponseDTO {
    private boolean success;
    private String linkAvatar;
}
