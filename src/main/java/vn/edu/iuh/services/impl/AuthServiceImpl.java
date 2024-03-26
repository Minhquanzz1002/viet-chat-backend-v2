package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.TokenResponseDTO;
import vn.edu.iuh.dto.RegisterRequestDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.UnauthorizedException;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.AuthService;
import vn.edu.iuh.utils.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public TokenResponseDTO login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository.findByPhone(loginRequestDTO.getPhone()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + loginRequestDTO.getPhone()));
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new UnauthorizedException(UserStatus.UNVERIFIED.getDescription());
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(UserStatus.LOCKED.getDescription());
        }
        if (passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            UserPrincipal userPrincipal = new UserPrincipal(user);
            String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);

            return TokenResponseDTO
                    .builder()
                    .refreshToken(refreshToken)
                    .accessToken(jwtUtil.generateAccessToken(userPrincipal))
                    .build();
        } else {
            throw new UnauthorizedException("Tài khoản hoặc mật khẩu không chính xác.");
        }
    }

    @Override
    public String register(RegisterRequestDTO registerRequestDTO, UserPrincipal userPrincipal) {
        User user = userRepository.findByPhone(userPrincipal.getUsername()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy thông tin người dùng"));
        if (user.getStatus() != UserStatus.UNVERIFIED) {
            throw new DataExistsException("Tài khoản đã đăng ký hoặc bị khóa");
        }
        user.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        UserInfo userInfo = new UserInfo(
                registerRequestDTO.getFirstName(),
                registerRequestDTO.getLastName(),
                registerRequestDTO.isGender(),
                registerRequestDTO.getBirthday(),
                user
        );
        userInfoRepository.save(userInfo);
        return "Cập nhật thông tin thành công";
    }

    @Override
    public TokenResponseDTO getAccessToken(UserPrincipal userPrincipal, String oldRefreshToken) {
        User user = userRepository.findByPhone(userPrincipal.getUsername()).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + userPrincipal.getUsername()));
        if (user.getStatus() == UserStatus.UNVERIFIED) {
            throw new UnauthorizedException(UserStatus.UNVERIFIED.getDescription());
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException(UserStatus.LOCKED.getDescription());
        }
        return TokenResponseDTO
                .builder()
                .refreshToken(jwtUtil.generateRefreshTokenFromOld(userPrincipal, oldRefreshToken))
                .accessToken(jwtUtil.generateAccessToken(userPrincipal))
                .build();
    }
}
