package vn.edu.iuh.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.RefreshToken;
@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
}
