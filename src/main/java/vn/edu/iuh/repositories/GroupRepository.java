package vn.edu.iuh.repositories;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.Group;

import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    @Cacheable(value = "groups", key = "#id")
    @Override
    Optional<Group> findById(String id);
}
