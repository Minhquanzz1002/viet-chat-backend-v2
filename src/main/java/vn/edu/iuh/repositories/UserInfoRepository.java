package vn.edu.iuh.repositories;

import org.bson.types.ObjectId;
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
    Optional<UserInfo> findByUser(User user);
    @Query("{'_id': ?0,'friends.friend_id': {$oid: ?1}}")
    Optional<UserInfo> findByIdAndFriendId(String id, String friendId);
    @Query("{'_id': ?0,'friends.friend_id': {$oid: ?1}, 'friends.status':  ?2}")
    Optional<UserInfo> findByIdAndFriendIdAndStatus(String id, String friendId, FriendStatus status);
    boolean existsByUser(User user);
}
