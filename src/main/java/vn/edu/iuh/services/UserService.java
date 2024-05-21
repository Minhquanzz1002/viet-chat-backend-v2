package vn.edu.iuh.services;

import vn.edu.iuh.models.User;

public interface UserService {
    boolean existsByPhone(String phone);
    User save(User user);
}
