package vn.edu.iuh.services;

import vn.edu.iuh.dto.LoginRequestDTO;
import vn.edu.iuh.dto.LoginResponseDTO;

public interface AuthService {
    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);
}
