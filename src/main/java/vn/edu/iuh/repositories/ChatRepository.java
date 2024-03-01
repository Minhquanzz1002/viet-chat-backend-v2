package vn.edu.iuh.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.Chat;
@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {
}
