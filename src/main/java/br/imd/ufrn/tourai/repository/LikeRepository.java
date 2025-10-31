package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<PostLike, Integer> {
    Optional<PostLike> findByPostIdAndLikerId(Integer postId, Integer userId);
}