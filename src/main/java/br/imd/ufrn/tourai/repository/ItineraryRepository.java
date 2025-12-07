package br.imd.ufrn.tourai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.imd.ufrn.tourai.model.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    @Query("""
        SELECT i
        FROM Itinerary i
        LEFT JOIN i.participants p
        WHERE i.user.id = :userId OR p.id = :userId
    """)
    public List<Itinerary> findByUserId(Long userId);

    Long countByUserId(Long userId);
}
