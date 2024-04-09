package vn.edu.iuh.repositories;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    @Cacheable(value = "users", key = "#phone")
    Optional<User> findByPhone(String phone);
    boolean existsByPhone(String phone);
    @CacheEvict(value = "users", key = "#entity.phone")
    @Override
    <S extends User> S save(S entity);
}
