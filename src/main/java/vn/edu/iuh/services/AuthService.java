package vn.edu.iuh.services;

import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.LoginResponseDTO;
import vn.edu.iuh.dto.RegisterRequestDTO;
import vn.edu.iuh.security.UserPrincipal;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
    String register(RegisterRequestDTO registerRequestDTO, UserPrincipal userPrincipal);
}
