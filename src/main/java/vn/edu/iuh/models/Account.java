package vn.edu.iuh.models;

import org.hibernate.validator.constraints.Length;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Account {
    @Id
    private String id;
    @Indexed
    @Length(min = 10, max = 10)
    private String phone;
    @Length(min = 8, max = 32)
    private String password;
    private boolean isPhoneVerified;
}
