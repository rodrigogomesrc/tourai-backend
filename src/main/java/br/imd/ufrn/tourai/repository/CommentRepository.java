package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {


    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.date DESC")
    List<Comment> findRecentByPost(@Param("postId") Integer postId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.date < :lastCommentDate ORDER BY c.date DESC")
    List<Comment> findOlderByPost(
            @Param("postId") Integer postId,
            @Param("lastCommentDate") Instant lastCommentDate,
            Pageable pageable
    );
}