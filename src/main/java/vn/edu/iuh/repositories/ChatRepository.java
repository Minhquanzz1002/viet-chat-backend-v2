package vn.edu.iuh.repositories;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import vn.edu.iuh.models.Chat;

import java.util.Optional;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {
    @Cacheable(value = "chats", key = "#id")
    @Override
    Optional<Chat> findById(String id);

    @Caching(
            put = {@CachePut(value = "chats", key = "#entity.id")},
            evict = {@CacheEvict(value = "profiles", allEntries = true)}
    )
    @Override
    <S extends Chat> S save(S entity);
}
