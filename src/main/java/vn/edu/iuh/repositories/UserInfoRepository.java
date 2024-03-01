package vn.edu.iuh.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.User;
import vn.edu.iuh.models.UserInfo;

import java.util.Optional;

@Repository
public interface UserInfoRepository extends MongoRepository<UserInfo, String> {
    Optional<UserInfo> findByUser(User user);

    boolean existsByUser(User user);
}
