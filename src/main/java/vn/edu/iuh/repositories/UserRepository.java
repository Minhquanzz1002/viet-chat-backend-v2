package vn.edu.iuh.repositories;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.User;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByPhone(String phone);
    @Cacheable(value = "users", key = "#id")
    @Override
    Optional<User> findById(String id);

    boolean existsByPhone(String phone);

    @CachePut(value = "users", key = "#entity.id")
    @Override
    <S extends User> S save(S entity);
}
