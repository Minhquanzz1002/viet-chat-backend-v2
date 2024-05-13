package vn.edu.iuh.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserChatUpdateDTO {
    private Boolean hidden;
    private Boolean pin;
}
