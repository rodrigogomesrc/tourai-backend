package br.imd.ufrn.tourai.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.imd.ufrn.tourai.model.Itinerary;
import br.imd.ufrn.tourai.service.ItineraryService;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

@RestController
@RequestMapping("/itineraries")
public class ItineraryController {
    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping
    public ResponseEntity<Itinerary> create(@RequestBody Itinerary itinerary) {
        Itinerary createdItinerary = itineraryService.save(itinerary);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItinerary);
    }

    @PutMapping("/{id}")
    public Itinerary update(@PathVariable Long id, @RequestBody Itinerary itinerary) {
        itinerary.setId(id);

        return itineraryService.save(itinerary);
    }

    @GetMapping
    public List<Itinerary> search(@RequestParam(required = true) Long userId) {
        return itineraryService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public Itinerary get(@PathVariable Long id) {
        return itineraryService.findByIdOrThrow(id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        itineraryService.deleteById(id);
    }
}
