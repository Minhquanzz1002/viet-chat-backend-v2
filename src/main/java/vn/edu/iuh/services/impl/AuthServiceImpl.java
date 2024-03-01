package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.LoginResponseDTO;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.UnauthorizedException;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.AuthService;
import vn.edu.iuh.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByPhone(loginRequestDTO.getPhone()).orElseThrow(()-> new DataNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + loginRequestDTO.getPhone()));
        if (user.getStatus() == UserStatus.PHONE_UNVERIFIED) {
            throw new UnauthorizedException(UserStatus.PHONE_UNVERIFIED.getDescription());
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(UserStatus.LOCKED.getDescription());
        }
        if (passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            UserPrincipal userPrincipal = new UserPrincipal(user);
            return LoginResponseDTO
                    .builder()
                    .refreshToken(jwtUtil.generateRefreshToken(userPrincipal))
                    .accessToken(jwtUtil.generateAccessToken(userPrincipal))
                    .build();
        }else{
            throw new UnauthorizedException("Tài khoản hoặc mật khẩu không chính xác.");
        }
    }
}
