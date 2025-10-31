package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.FavoriteRoadmap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRoadmapRepository extends JpaRepository<FavoriteRoadmap, Integer> {

    Optional<FavoriteRoadmap> findByRoadmapIdAndUserId(Long roadmapId, Long userId);

    List<FavoriteRoadmap> findByUserId(Long userId);

}
