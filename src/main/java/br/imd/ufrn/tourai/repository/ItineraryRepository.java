package br.imd.ufrn.tourai.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.imd.ufrn.tourai.model.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    @Query("""
        SELECT i
        FROM Itinerary i
        LEFT JOIN i.participants p
        WHERE (i.user.id = :userId OR p.id = :userId)
        AND (
            :type IS NULL OR
            (:type = 'OWNED' AND i.user.id = :userId) OR
            (:type = 'PARTICIPATING' AND p.id = :userId)
        )
        AND (
            :search IS NULL
            OR LOWER(i.roadmap.title) LIKE %:search%
            OR LOWER(i.roadmap.description) LIKE %:search%
        )
    """)
    public Page<Itinerary> findByUserId(
        Long userId,
        String type,
        String search,
        Pageable pageable
    );

    Long countByUserId(Long userId);
}
