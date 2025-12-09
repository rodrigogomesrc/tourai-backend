package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Verifica se o usuário A já segue o usuário B
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Busca o relacionamento específico para deletar (unfollow)
    Optional<Follow> findByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // Conta quantos seguidores um usuário tem
    Long countByFollowingId(Long userId);

    // Conta quantas pessoas o usuário está seguindo
    Long countByFollowerId(Long userId);
}
