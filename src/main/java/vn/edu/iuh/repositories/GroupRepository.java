package vn.edu.iuh.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.Group;
@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
}
