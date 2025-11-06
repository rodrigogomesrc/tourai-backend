package br.imd.ufrn.tourai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.imd.ufrn.tourai.model.Evaluation;

public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {
    Optional<Evaluation> findByItineraryActivityId(Integer itineraryActivityId);
    void deleteByItineraryActivityId(Integer itineraryActivityId);
}
