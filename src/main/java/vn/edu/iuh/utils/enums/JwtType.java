package vn.edu.iuh.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum JwtType {
    ACCESS_TOKEN, REFRESH_TOKEN, RESET_TOKEN, REGISTER_TOKEN;
}
