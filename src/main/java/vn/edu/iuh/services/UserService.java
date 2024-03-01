package vn.edu.iuh.services;

import vn.edu.iuh.models.User;

public interface UserService {
    User findByPhone(String phone);
    boolean existsByPhone(String phone);
    User save(User user);
}
