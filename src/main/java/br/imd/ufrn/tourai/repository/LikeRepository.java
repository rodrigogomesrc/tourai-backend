package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<CommentLike, Integer> {
    Optional<CommentLike> findByPostIdAndLikerId(Integer postId, Integer userId);
}