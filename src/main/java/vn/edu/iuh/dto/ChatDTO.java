package vn.edu.iuh.dto;

import lombok.*;
import vn.edu.iuh.models.Chat;
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO extends Chat {
    private String name;
    private String avatar;
}
