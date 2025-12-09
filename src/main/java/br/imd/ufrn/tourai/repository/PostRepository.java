package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT p FROM Post p " +
            "WHERE (:search IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.user.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.postDate DESC")
    List<Post> findLast(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.postDate < :dateLastPost " +
            "AND (:search IS NULL OR LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.user.name) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "ORDER BY p.postDate DESC")
    List<Post> findOlder(@Param("dateLastPost") Instant dateLastPost, @Param("search") String search, Pageable pageable);

    Long countByUserId(Long userId);
}
