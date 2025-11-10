package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Roadmap;
import br.imd.ufrn.tourai.model.ModerationStatus;
import br.imd.ufrn.tourai.model.RoadmapVisibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    List<Roadmap> findByOwnerIdOrderByTitleAsc(Long userId);

    List<Roadmap> findByVisibilityAndStatus(RoadmapVisibility visibility, ModerationStatus status);

    @Query("SELECT r FROM Roadmap r LEFT JOIN FETCH r.activities WHERE r.id = :id")
    Roadmap findByIdWithActivities(Long id);

    @Query("SELECT r FROM Roadmap r WHERE r.id IN " +
            "(SELECT fr.roadmap.id FROM FavoriteRoadmap fr WHERE fr.user.id = :userId) " +
            "ORDER BY r.title ASC")
    List<Roadmap> findFavoritedByUserId(@Param("userId") Long userId);


}

