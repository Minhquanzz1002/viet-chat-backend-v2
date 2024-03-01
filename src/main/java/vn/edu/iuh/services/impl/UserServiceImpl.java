package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.models.User;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.services.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public User findByPhone(String phone) {
        return userRepository.findByPhone(phone).orElseThrow(() -> new DataNotFoundException("Account not found with phone: " + phone));
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }


}
