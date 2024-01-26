package vn.edu.iuh.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class PhoneNumberDTO {
//    @Length(min = 1, max = 10)
    private String phone;
}
