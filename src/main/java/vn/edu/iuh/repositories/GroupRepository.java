package vn.edu.iuh.repositories;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.Group;

import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group, String> {
    @Cacheable(value = "groups", key = "#id")
    @Override
    Optional<Group> findById(String id);

    @Caching(
            put = {
                    @CachePut(value = "groups", key = "#p0.id")
            }
    )
    @Override
    <S extends Group> S save(S entity);
}
