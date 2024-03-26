package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vn.edu.iuh.models.RefreshToken;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.RefreshTokenStatus;
import vn.edu.iuh.repositories.RefreshTokenRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.services.RefreshTokenService;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;


    @Override
    public RefreshToken create(String phone) {
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm người dùng nào có số điện thoại là " + phone.substring(phone.length() - 3)));
        RefreshToken refreshToken = RefreshToken
                .builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusDays(14))
                .status(RefreshTokenStatus.ACTIVE)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }
}
