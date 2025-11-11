package br.imd.ufrn.tourai.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.imd.ufrn.tourai.model.Itinerary;

public interface ItineraryRepository extends JpaRepository<Itinerary, Long> {
    public List<Itinerary> findByUserId(Long userId);

    Long countByUserId(Long userId);
}
