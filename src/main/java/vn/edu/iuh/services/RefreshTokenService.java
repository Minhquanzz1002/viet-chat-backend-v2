package vn.edu.iuh.services;

import vn.edu.iuh.models.RefreshToken;

public interface RefreshTokenService {
    RefreshToken create(String phone);

}
