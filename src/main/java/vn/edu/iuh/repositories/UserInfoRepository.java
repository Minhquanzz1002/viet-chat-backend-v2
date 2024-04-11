package vn.edu.iuh.repositories;

import org.bson.types.ObjectId;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.UserInfo;
import vn.edu.iuh.models.enums.FriendStatus;
import vn.edu.iuh.models.enums.UserStatus;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    @Cacheable(value = "profiles", key = "#user.id")
    Optional<UserInfo> findByUser(User user);
    @Caching(
            put = {
                    @CachePut(value = "profiles", key = "#entity.user.id"),
                    @CachePut(value = "profiles", key = "#entity.id")
            }
    )
    @Override
    <S extends UserInfo> S save(S entity);
    @Cacheable(value = "profiles", key = "#id")
    @Override
    Optional<UserInfo> findById(String id);
}
