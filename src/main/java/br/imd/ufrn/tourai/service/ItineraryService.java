package br.imd.ufrn.tourai.service;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

        Set<String> seenTimes = new HashSet<>();

        for (CreateItineraryRequest.CreateItineraryRequestActivity activity : request.activities()) {
            OffsetDateTime normalized = activity.time()
                .withSecond(0)
                .withNano(0);

            String key = normalized.toInstant().toString();

            if (!seenTimes.add(key)) {
                throw new IllegalArgumentException(
                    "There are activities with the same time: " + normalized
                );
            }
        }

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

        Set<String> seenTimes = new HashSet<>();

        if (request.activities() != null) {
            for (ItineraryActivity activity : itinerary.getActivities()) {
                Optional<UpdateItineraryRequest.UpdateItineraryRequestActivity> updateAcitivty = request.activities()
                    .stream()
                    .filter(other -> other.activityId() == activity.getActivity().getId())
                    .findFirst();

                if (updateAcitivty.isPresent()) {
                    if (updateAcitivty.get().time() != null) {
                        activity.setTime(updateAcitivty.get().time());
                    }

                    if (updateAcitivty.get().completed() != null) {
                        activity.setCompleted(updateAcitivty.get().completed());
                    }
                }

                OffsetDateTime normalized = activity.getTime()
                    .withSecond(0)
                    .withNano(0);

                String key = normalized.toInstant().toString();

                if (!seenTimes.add(key)) {
                    throw new IllegalArgumentException(
                        "There are activities with the same time: " + normalized
                    );
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

    public Long countByUserId(Long userId) {
        return itineraryRepository.countByUserId(userId);
    }
}
