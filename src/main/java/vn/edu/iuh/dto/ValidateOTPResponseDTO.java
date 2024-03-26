package vn.edu.iuh.dto;

import lombok.*;

@Builder
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ValidateOTPResponseDTO {
    private String accessToken;
    private String refreshToken;
}