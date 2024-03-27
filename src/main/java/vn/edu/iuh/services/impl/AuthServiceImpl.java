package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.TokenResponseDTO;
import vn.edu.iuh.dto.RegisterRequestDTO;
import vn.edu.iuh.exceptions.DataExistsException;
import vn.edu.iuh.exceptions.DataNotFoundException;
import vn.edu.iuh.exceptions.UnauthorizedException;
import vn.edu.iuh.models.RefreshToken;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.RefreshTokenStatus;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.RefreshTokenRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.AuthService;
import vn.edu.iuh.utils.JwtUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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
            RefreshToken refreshToken = RefreshToken
                    .builder()
                    .token(jwtUtil.generateRefreshToken(userPrincipal))
                    .status(RefreshTokenStatus.ACTIVE)
                    .user(user)
                    .build();
            refreshTokenRepository.save(refreshToken);
            return TokenResponseDTO
                    .builder()
                    .refreshToken(refreshToken.getToken())
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
    public TokenResponseDTO getAccessToken(String oldRefreshToken) {
        if (!jwtUtil.isTokenExpired(oldRefreshToken) && jwtUtil.isRefreshToken(oldRefreshToken)) {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(oldRefreshToken).orElseThrow(() -> new BadCredentialsException("Token không tìm thấy hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới"));
            if (refreshToken.getStatus() != RefreshTokenStatus.ACTIVE) {
                throw new BadCredentialsException("Token đã hết hạn. Hãy đăng nhập lại để lấy token mới");
            }
            String username = jwtUtil.extractUsername(oldRefreshToken);
            User user = userRepository.findByPhone(username).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + username.substring(username.length() - 3)));
            if (user.getStatus() == UserStatus.UNVERIFIED) {
                throw new UnauthorizedException(UserStatus.UNVERIFIED.getDescription());
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException(UserStatus.LOCKED.getDescription());
            }

            UserPrincipal userPrincipal = new UserPrincipal(user);
            refreshToken.setToken(jwtUtil.generateRefreshTokenFromOld(userPrincipal, oldRefreshToken));
            refreshTokenRepository.save(refreshToken);
            return TokenResponseDTO
                    .builder()
                    .refreshToken(refreshToken.getToken())
                    .accessToken(jwtUtil.generateAccessToken(userPrincipal))
                    .build();
        } else {
            throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới");
        }
    }

    @Override
    public String logout(String token) {
        if (!jwtUtil.isTokenExpired(token) && jwtUtil.isRefreshToken(token)) {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new BadCredentialsException("Token không tìm thấy hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới"));
            if (refreshToken.getStatus() != RefreshTokenStatus.ACTIVE) {
                throw new BadCredentialsException("Token đã hết hạn. Hãy đăng nhập lại để lấy token mới");
            }
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByPhone(username).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + username.substring(username.length() - 3)));
            if (user.getStatus() == UserStatus.UNVERIFIED) {
                throw new UnauthorizedException(UserStatus.UNVERIFIED.getDescription());
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException(UserStatus.LOCKED.getDescription());
            }
            refreshToken.setStatus(RefreshTokenStatus.LOGOUT);
            refreshTokenRepository.save(refreshToken);
            return "Đăng xuất thành công";
        } else {
            throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới");
        }
    }

    @Override
    public String logoutAll(String token) {
        if (!jwtUtil.isTokenExpired(token) && jwtUtil.isRefreshToken(token)) {
            RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElseThrow(() -> new BadCredentialsException("Token không tìm thấy hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới"));
            if (refreshToken.getStatus() != RefreshTokenStatus.ACTIVE) {
                throw new BadCredentialsException("Token đã hết hạn. Hãy đăng nhập lại để lấy token mới");
            }
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByPhone(username).orElseThrow(() -> new DataNotFoundException("Không tìm thấy người dùng nào có số điện thoại là " + username.substring(username.length() - 3)));
            if (user.getStatus() == UserStatus.UNVERIFIED) {
                throw new UnauthorizedException(UserStatus.UNVERIFIED.getDescription());
            }
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UnauthorizedException(UserStatus.LOCKED.getDescription());
            }

            List<RefreshToken> refreshTokens = refreshTokenRepository.findAllByUserAndStatusAndTokenNot(user, RefreshTokenStatus.ACTIVE, token);
            refreshTokens.forEach(rt -> {
                rt.setStatus(RefreshTokenStatus.LOGOUT);
                refreshTokenRepository.save(rt);
            });

            return "Đăng xuất trên các thiết bị khác thành công";
        } else {
            throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới");
        }
    }
}
