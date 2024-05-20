package vn.edu.iuh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {
    @NotNull(message = "Họ là bắt buộc")
    @NotBlank(message = "Họ không được rỗng")
    @Pattern(regexp = "^[A-ZÀÁẢẠÃĂẰẮẲẶẴÂẦẤẨẬẪĐEÈÉẺẸẼÊỀẾỂỆỄIÌÍỈỊĨOÒÓỎỌÕÔỒỐỔỘỖƠỜỚỞỢỠUÙÚỦỤŨƯỪỨỬỰỮYỲÝỶỴỸa-zàáảạãăằắẳặẵâầấẩậẫđeèéẻẹẽêềếểệễiìíỉịĩoòóỏọõôồốổộỗơờớởợỡuùúủụũưừứửựữyỳýỷỵỹ?][A-ZÀÁẢẠÃĂẰẮẲẶẴÂẦẤẨẬẪĐEÈÉẺẸẼÊỀẾỂỆỄIÌÍỈỊĨOÒÓỎỌÕÔỒỐỔỘỖƠỜỚỞỢỠUÙÚỦỤŨƯỪỨỬỰỮYỲÝỶỴỸa-zàáảạãăằắẳặẵâầấẩậẫđeèéẻẹẽêềếểệễiìíỉịĩoòóỏọõôồốổộỗơờớởợỡuùúủụũưừứửựữyỳýỷỵỹ?\\s]*$", message = "Họ chỉ gồm chữ cái và khoảng trắng")
    private String firstName;
    @NotNull(message = "Tên là bắt buộc")
    @NotBlank(message = "Tên không được rỗng")
    @Pattern(regexp = "^[A-ZÀÁẢẠÃĂẰẮẲẶẴÂẦẤẨẬẪĐEÈÉẺẸẼÊỀẾỂỆỄIÌÍỈỊĨOÒÓỎỌÕÔỒỐỔỘỖƠỜỚỞỢỠUÙÚỦỤŨƯỪỨỬỰỮYỲÝỶỴỸa-zàáảạãăằắẳặẵâầấẩậẫđeèéẻẹẽêềếểệễiìíỉịĩoòóỏọõôồốổộỗơờớởợỡuùúủụũưừứửựữyỳýỷỵỹ?][A-ZÀÁẢẠÃĂẰẮẲẶẴÂẦẤẨẬẪĐEÈÉẺẸẼÊỀẾỂỆỄIÌÍỈỊĨOÒÓỎỌÕÔỒỐỔỘỖƠỜỚỞỢỠUÙÚỦỤŨƯỪỨỬỰỮYỲÝỶỴỸa-zàáảạãăằắẳặẵâầấẩậẫđeèéẻẹẽêềếểệễiìíỉịĩoòóỏọõôồốổộỗơờớởợỡuùúủụũưừứửựữyỳýỷỵỹ?\\s]*$", message = "Tên chỉ gồm chữ cái và khoảng trắng")
    private String lastName;
    @NotNull(message = "Giới tính là bắt buộc")
    private boolean gender;
    @NotNull(message = "Ngày sinh là bắt buộc")
    @Past(message = "Ngày sinh phải trước ngày hiện tại")
    private LocalDate birthday;
    @NotNull(message = "Mật khẩu là bắt buộc")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=])(?=\\S+$).{8,32}$", message = "Mật khẩu từ 8 - 32 ký tự gồm tối thiểu 1 chữ cái viết hoa, 1 chữ cái viết thường, 1 chữ số và 1 ký tự đặc biệt")
    private String password;
}
