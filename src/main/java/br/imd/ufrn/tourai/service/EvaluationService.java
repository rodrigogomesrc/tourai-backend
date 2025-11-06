package br.imd.ufrn.tourai.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.imd.ufrn.tourai.dto.EvaluationRequest;
import br.imd.ufrn.tourai.exception.BadRequestException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.Evaluation;
import br.imd.ufrn.tourai.model.ItineraryActivity;
import br.imd.ufrn.tourai.repository.EvaluationRepository;
import br.imd.ufrn.tourai.repository.ItineraryActivityRepository;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ItineraryActivityRepository itineraryActivityRepository;

    public EvaluationService(EvaluationRepository evaluationRepository, ItineraryActivityRepository itineraryActivityRepository) {
        this.evaluationRepository = evaluationRepository;
        this.itineraryActivityRepository = itineraryActivityRepository;
    }

    private void validateRating(Integer rating) {
        if (rating == null) throw new BadRequestException("Rating is required");
        if (rating < 0 || rating > 10) throw new BadRequestException("Rating must be between 0 and 10");
    }

    @Transactional
    public Evaluation create(Integer itineraryActivityId, EvaluationRequest request) {
        Integer rating = request.getRating();
        validateRating(rating);

        ItineraryActivity activity = itineraryActivityRepository.findById(itineraryActivityId)
                .orElseThrow(() -> new ResourceNotFoundException("ItineraryActivity not found: " + itineraryActivityId));
        if (evaluationRepository.findByItineraryActivityId(itineraryActivityId).isPresent()) {
            throw new BadRequestException("Evaluation already exists for this itinerary activity");
        }

        Evaluation newEvaluation = new Evaluation(rating, request.getComment(), activity);
        return evaluationRepository.save(newEvaluation);
    }

    @Transactional
    public Evaluation update(Integer itineraryActivityId, EvaluationRequest request) {
        validateRating(request.getRating());

        Evaluation evaluation = evaluationRepository.findByItineraryActivityId(itineraryActivityId)
                .orElseThrow(() -> new ResourceNotFoundException("Evaluation not found for itinerary activity: " + itineraryActivityId));

        evaluation.setRating(request.getRating());
        evaluation.setComment(request.getComment());
        return evaluationRepository.save(evaluation);
    }

    public Optional<Evaluation> findByItineraryActivityId(Integer itineraryActivityId) {
        return evaluationRepository.findByItineraryActivityId(itineraryActivityId);
    }

    @Transactional
    public void deleteByItineraryActivityId(Integer itineraryActivityId) {
        evaluationRepository.deleteByItineraryActivityId(itineraryActivityId);
    }
}
