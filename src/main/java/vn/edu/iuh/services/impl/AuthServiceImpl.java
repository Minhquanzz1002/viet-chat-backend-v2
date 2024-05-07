package vn.edu.iuh.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.edu.iuh.dto.*;
import vn.edu.iuh.exceptions.*;
import vn.edu.iuh.models.RefreshToken;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.RefreshTokenStatus;
import vn.edu.iuh.models.enums.UserStatus;
import vn.edu.iuh.repositories.OTPRepository;
import vn.edu.iuh.repositories.RefreshTokenRepository;
import vn.edu.iuh.repositories.UserInfoRepository;
import vn.edu.iuh.repositories.UserRepository;
import vn.edu.iuh.security.UserPrincipal;
import vn.edu.iuh.services.AuthService;
import vn.edu.iuh.utils.JwtUtil;
import vn.edu.iuh.utils.enums.JwtType;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final UserInfoRepository userInfoRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final OTPRepository otpRepository;
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
        if (!jwtUtil.isTokenExpired(oldRefreshToken) && jwtUtil.extractTokenType(oldRefreshToken).equals(JwtType.REFRESH_TOKEN)) {
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
        }
        throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới");
    }

    @Override
    public String logout(String token) {
        if (!jwtUtil.isTokenExpired(token) && jwtUtil.extractTokenType(token).equals(JwtType.REFRESH_TOKEN)) {
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
        }
        throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới");
    }

    @Override
    public String logoutAll(String token) {
        if (!jwtUtil.isTokenExpired(token) && jwtUtil.extractTokenType(token).equals(JwtType.REFRESH_TOKEN)) {
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
        }
        throw new BadCredentialsException("Token không hợp lệ hoặc đã hết hạn. Hãy đăng nhập lại để lấy token mới");
    }

    @Override
    public String forgotPassword(String phone) {
        String phoneSuffix = phone.substring(phone.length() - 3);
        User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản có số điện thoại là ***" + phoneSuffix));
        if (user.getStatus() == UserStatus.LOCKED) {
            throw new DataExistsException("Tài khoản ***" + phoneSuffix + " đã bị khóa.");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new DataExistsException("Tài khoản ***" + phoneSuffix + " không sẵn sàng.");
        }
        String otp = otpRepository.generateOTP(phone);
        log.info("Mã OTP để reset password của số điện thoại {} bạn là {}", phone, otp);
        return "Gửi OTP thành công. Hãy xác thực để tiếp tục";
    }

    @Override
    public ResetTokenDTO validateResetPassword(String phone, String otp) {
        boolean isValid = otpRepository.validateOTP(phone, otp);
        if (isValid) {
            User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user nào có số điện thoại là ***" + phone.substring(phone.length() - 3)));
            return new ResetTokenDTO(jwtUtil.generateResetToken(new UserPrincipal(user)));
        }
        throw new OTPMismatchException("OTP không chính xác hoặc đã hết hạn");
    }

    @Override
    public String resetPassword(String token, String password) {
        if (!jwtUtil.isTokenExpired(token) && jwtUtil.extractTokenType(token).equals(JwtType.RESET_TOKEN)) {
            String phone = jwtUtil.extractUsername(token);
            User user = userRepository.findByPhone(phone).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user nào có số điện thoại là ***" + phone.substring(phone.length() - 3)));
            user.setPassword(passwordEncoder.encode(password));
            userRepository.save(user);
            return "Cập nhật mật khẩu thành công.";
        }
        throw new OTPMismatchException("OTP không chính xác hoặc đã hết hạn");
    }

    @Override
    public String changePassword(ChangePasswordRequestDTO changePasswordRequestDTO, UserPrincipal userPrincipal) {
        String phoneSuffix = userPrincipal.getUsername().substring(userPrincipal.getUsername().length() - 3);
        User user = userRepository.findByPhone(userPrincipal.getUsername()).orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản có số điện thoại là ***" + phoneSuffix));
        if(passwordEncoder.matches(changePasswordRequestDTO.getOldPassword(), user.getPassword())){
            user.setPassword(passwordEncoder.encode(changePasswordRequestDTO.getNewPassword()));
            userRepository.save(user);
            return "Cập nhật mật khẩu thành công.";
        }
        throw new InvalidRequestException("Mật khẩu hiện tại không chính xác. Vui lòng nhập lại");
    }
}
