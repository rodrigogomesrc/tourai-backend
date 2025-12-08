package br.imd.ufrn.tourai.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.imd.ufrn.tourai.model.Invite;

public interface InviteRepository extends JpaRepository<Invite, Long> {
    @Query("SELECT i FROM Invite i WHERE i.itinerary.id = :itineraryId AND i.user.id = :userId")
    public Optional<Invite> findByItineraryIdAndUserId(Long itineraryId, Long userId);

    public List<Invite> findByUserId(Long userId);
}
