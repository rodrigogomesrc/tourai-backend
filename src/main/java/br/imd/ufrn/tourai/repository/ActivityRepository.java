package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Activity;
import br.imd.ufrn.tourai.model.ActivityType;
import br.imd.ufrn.tourai.model.ModerationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Long> {

    @Query("SELECT a FROM Activity a WHERE (a.type = :systemType OR (a.type = :publicType AND a.moderationStatus = :approvedStatus)) AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Activity> findPublicByName(@Param("systemType") ActivityType systemType,
                                    @Param("publicType") ActivityType publicType,
                                    @Param("approvedStatus") ModerationStatus approvedStatus,
                                    @Param("name") String name,
                                    Pageable pageable);

    @Query("SELECT DISTINCT a FROM Activity a JOIN a.tags t WHERE (a.type = :systemType OR (a.type = :publicType AND a.moderationStatus = :approvedStatus)) AND LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%')) AND t IN :tags")
    Page<Activity> findPublicByNameAndTags(@Param("systemType") ActivityType systemType,
                                           @Param("publicType") ActivityType publicType,
                                           @Param("approvedStatus") ModerationStatus approvedStatus,
                                           @Param("name") String name,
                                           @Param("tags") Collection<String> tags,
                                           Pageable pageable);

    List<Activity> findByCreatorId(Long creatorId);

    List<Activity> findByTypeAndModerationStatus(ActivityType type, ModerationStatus status);
}

