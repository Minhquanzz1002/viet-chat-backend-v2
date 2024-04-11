package vn.edu.iuh.repositories;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.RefreshToken;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.enums.RefreshTokenStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    @Cacheable(value = "tokens", key = "#token")
    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findAllByUserAndStatusAndTokenNot(User user, RefreshTokenStatus status, String token);
}
