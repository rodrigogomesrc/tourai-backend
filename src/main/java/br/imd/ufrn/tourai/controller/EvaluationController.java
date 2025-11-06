package br.imd.ufrn.tourai.controller;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.imd.ufrn.tourai.dto.EvaluationRequest;
import br.imd.ufrn.tourai.dto.EvaluationResponse;
import br.imd.ufrn.tourai.model.Evaluation;
import br.imd.ufrn.tourai.service.EvaluationService;

@RestController
@RequestMapping("/itineraries/{activityId}/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<EvaluationResponse> create(@PathVariable("activityId") Integer activityId, @RequestBody EvaluationRequest request) {
        Evaluation saved = evaluationService.create(activityId, request);
        EvaluationResponse response = new EvaluationResponse(saved.getId(), saved.getRating(), saved.getComment(), saved.getItineraryActivity().getId());
        URI location = URI.create(String.format("/itineraries/%d/evaluation", activityId));
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<EvaluationResponse> get(@PathVariable("activityId") Integer activityId) {
        return evaluationService.findByItineraryActivityId(activityId)
                .map(e -> ResponseEntity.ok(new EvaluationResponse(e.getId(), e.getRating(), e.getComment(), e.getItineraryActivity().getId())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<EvaluationResponse> update(@PathVariable("activityId") Integer activityId, @RequestBody EvaluationRequest request) {
        Evaluation updated = evaluationService.update(activityId, request);
        return ResponseEntity.ok(new EvaluationResponse(updated.getId(), updated.getRating(), updated.getComment(), updated.getItineraryActivity().getId()));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable("activityId") Integer activityId) {
        evaluationService.deleteByItineraryActivityId(activityId);
        return ResponseEntity.noContent().build();
    }
}
