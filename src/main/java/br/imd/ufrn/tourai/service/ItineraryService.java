package br.imd.ufrn.tourai.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.imd.ufrn.tourai.dto.CreateItineraryRequest;
import br.imd.ufrn.tourai.dto.UpdateItineraryRequest;
import br.imd.ufrn.tourai.exception.BadRequestException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.Activity;
import br.imd.ufrn.tourai.model.Itinerary;
import br.imd.ufrn.tourai.model.ItineraryActivity;
import br.imd.ufrn.tourai.model.Roadmap;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.ActivityRepository;
import br.imd.ufrn.tourai.repository.ItineraryRepository;
import br.imd.ufrn.tourai.repository.RoadmapRepository;
import br.imd.ufrn.tourai.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class ItineraryService {
    private final ItineraryRepository itineraryRepository;
    private final UserRepository userRepository;
    private final RoadmapRepository roadmapRepository;
    private final ActivityRepository activityRepository;


    public ItineraryService(
        ItineraryRepository itineraryRepository,
        UserRepository userRepository,
        RoadmapRepository roadmapRepository,
        ActivityRepository activityRepository
    ) {
        this.itineraryRepository = itineraryRepository;
        this.userRepository = userRepository;
        this.roadmapRepository = roadmapRepository;
        this.activityRepository = activityRepository;
    }

    private Itinerary fromDTOToEntity(CreateItineraryRequest request) {
        User user = userRepository
            .findById(request.userId())
            .orElseThrow(() -> new ResourceNotFoundException("User with id " + request.userId() + " not found"));

        Roadmap roadmap = roadmapRepository
            .findById(request.roadmapId())
            .orElseThrow(() -> new ResourceNotFoundException("Roadmap with id " + request.roadmapId() + " not found"));

        List<ItineraryActivity> activities = request.activities()
            .stream()
            .map((CreateItineraryRequest.CreateItineraryRequestActivity item) -> {
                Activity activity = activityRepository
                    .findById(item.activityId())
                    .orElseThrow(() -> new ResourceNotFoundException("Activity with id " + item.activityId() + " not found"));

                ItineraryActivity itineraryActivity = new ItineraryActivity();

                itineraryActivity.setActivity(activity);
                itineraryActivity.setTime(item.time());

                return itineraryActivity;
            })
            .toList();

        Itinerary itinerary = new Itinerary();

        itinerary.setUser(user);
        itinerary.setRoadmap(roadmap);
        itinerary.setActivities(activities);
        activities.forEach(a -> a.setItinerary(itinerary));

        return itinerary;
    }

    @Transactional
    public Itinerary create(CreateItineraryRequest request) {
        Itinerary itinerary = fromDTOToEntity(request);

        for (Activity activity : itinerary.getRoadmap().getActivities()) {
            boolean hasActivity = itinerary.getActivities()
                .stream()
                .anyMatch((itineraryActivity) -> itineraryActivity.getActivity().getId() == activity.getId());

            if (!hasActivity) {
                throw new BadRequestException("Itinerary must have exactly all roadmap activities");
            }
        }

        if (itinerary.getActivities().size() != itinerary.getRoadmap().getActivities().size()) {
            throw new BadRequestException("Itinerary must have exactly all roadmap activities");
        }

        return itineraryRepository.save(itinerary);
    }

    @Transactional
    public Itinerary update(Long id, UpdateItineraryRequest request) {
        Itinerary itinerary = itineraryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Itinerary with id " + id + " not found"));

        if (request.activities() != null) {
            for (ItineraryActivity activity : itinerary.getActivities()) {
                Optional<UpdateItineraryRequest.UpdateItineraryRequestActivity> updateAcitivty = request.activities()
                    .stream()
                    .filter(other -> other.itineraryActivityId() == activity.getId())
                    .findFirst();

                if (updateAcitivty.isPresent()) {
                    activity.setTime(updateAcitivty.get().time());
                }
            }
        }

        return itineraryRepository.save(itinerary);
    }

    public List<Itinerary> findByUserId(Long userId) {
        return itineraryRepository.findByUserId(userId);
    }

    public Itinerary findByIdOrThrow(Long id) {
        return itineraryRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Itinerary with id " + id + " not found"));
    }

    public void deleteById(Long id) {
        itineraryRepository.deleteById(id);
    }
}
