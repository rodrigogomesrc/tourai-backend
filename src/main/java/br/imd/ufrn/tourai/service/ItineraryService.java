package br.imd.ufrn.tourai.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.imd.ufrn.tourai.model.Itinerary;
import br.imd.ufrn.tourai.repository.ItineraryRepository;
import jakarta.persistence.EntityNotFoundException;

@Service
public class ItineraryService {
    private final ItineraryRepository itineraryRepository;


    public ItineraryService(ItineraryRepository itineraryRepository) {
        this.itineraryRepository = itineraryRepository;
    }

    public Itinerary save(Itinerary itinerary) {
        itinerary.getActivities().forEach(a -> a.setItinerary(itinerary));

        return itineraryRepository.save(itinerary);
    }

    public List<Itinerary> findByUserId(Long userId) {
        return itineraryRepository.findByUserId(userId);
    }

    public Itinerary findByIdOrThrow(Long id) {
        return itineraryRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Itinerary with id " + id + " not found"));
    }

    public void deleteById(Long id) {
        itineraryRepository.deleteById(id);
    }
}
