package br.imd.ufrn.tourai.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.imd.ufrn.tourai.config.CustomUserDetails;
import br.imd.ufrn.tourai.dto.CreateItineraryRequest;
import br.imd.ufrn.tourai.dto.ItineraryType;
import br.imd.ufrn.tourai.dto.UpdateItineraryRequest;
import br.imd.ufrn.tourai.model.Itinerary;
import br.imd.ufrn.tourai.service.ItineraryService;

@RestController
@RequestMapping("/itineraries")
public class ItineraryController {
    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @PostMapping
    public ResponseEntity<Itinerary> create(@RequestBody CreateItineraryRequest itinerary) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        Itinerary createdItinerary = itineraryService.create(userDetails, itinerary);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdItinerary);
    }

    @PutMapping("/{id}")
    public Itinerary update(@PathVariable Long id, @RequestBody UpdateItineraryRequest itinerary) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return itineraryService.update(userDetails, id, itinerary);
    }

    @GetMapping
    public Page<Itinerary> search(
        @RequestParam(required = false) ItineraryType type,
        @RequestParam(required = false) String search,
        Pageable pageable
    ) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return itineraryService.find(userDetails, type, search, pageable);
    }

    @GetMapping("/{id}")
    public Itinerary get(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return itineraryService.findByIdOrThrow(userDetails, id);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        itineraryService.deleteById(userDetails, id);
    }
}
