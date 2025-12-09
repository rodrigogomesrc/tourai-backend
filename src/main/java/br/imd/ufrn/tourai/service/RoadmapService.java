package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.model.*;
import br.imd.ufrn.tourai.repository.RoadmapRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    @Autowired
    private RoadmapRepository roadmapRepository;

    @Autowired
    private ActivityService activityService;

    @Transactional(readOnly = true)
    public Page<Roadmap> listMyRoadmaps(User loggedUser, Pageable pageable) {
        if (loggedUser == null || loggedUser.getId() == null) {
            throw new IllegalArgumentException("Invalid or not logged user.");
        }
        return roadmapRepository.findByOwnerIdOrderByTitleAsc(loggedUser.getId(), pageable);
    }

    @Transactional
    public Roadmap createRoadmap(Roadmap newRoadmap, User loggedUser) {
        if (loggedUser == null || loggedUser.getId() == null) {
            throw new IllegalArgumentException("User must be logged in to create roadmap.");
        }
        if (!StringUtils.hasText(newRoadmap.getTitle())) {
            throw new IllegalArgumentException("Roadmap title cannot be empty.");
        }
        Roadmap r = new Roadmap();
        r.setTitle(newRoadmap.getTitle());
        r.setDescription(newRoadmap.getDescription());
        if (newRoadmap.getTags() != null) {
            r.setTags(new HashSet<>(newRoadmap.getTags()));
        }
        r.setOwner(loggedUser);
        r.setVisibility(newRoadmap.getVisibility() != null ? newRoadmap.getVisibility() : RoadmapVisibility.PRIVATE);
        if (r.getVisibility() == RoadmapVisibility.PUBLIC) {
            r.setStatus(ModerationStatus.PENDING);
        } else {
            r.setStatus(null);
        }
        if (newRoadmap.getActivities() != null && !newRoadmap.getActivities().isEmpty()) {
            Set<Long> activityIds = newRoadmap.getActivities().stream()
                    .map(Activity::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Activity> toAdd = activityIds.stream()
                    .map(activityService::findByIdOrThrow)
                    .collect(Collectors.toSet());
            toAdd.forEach(r::addActivity);
        }
        return roadmapRepository.save(r);
    }

    @Transactional
    public Roadmap updateRoadmap(Long id, Roadmap update, User loggedUser) {
        Roadmap existing = findByIdAndOwnerOrThrow(id, loggedUser);
        if (StringUtils.hasText(update.getTitle())) {
            existing.setTitle(update.getTitle());
        }
        existing.setDescription(update.getDescription());
        if (update.getTags() != null) {
            existing.setTags(new HashSet<>(update.getTags()));
        }
        RoadmapVisibility oldVis = existing.getVisibility();
        RoadmapVisibility newVis = update.getVisibility();
        if (newVis != null && oldVis != newVis) {
            existing.setVisibility(newVis);
            if (newVis == RoadmapVisibility.PUBLIC) {
                if (existing.getStatus() != ModerationStatus.APPROVED) {
                    existing.setStatus(ModerationStatus.PENDING);
                }
            } else {
                existing.setStatus(null);
            }
        }
        return roadmapRepository.save(existing);
    }

    @Transactional
    public void deleteRoadmap(Long id, User loggedUser) {
        Roadmap roadmap = findByIdAndOwnerOrThrow(id, loggedUser);
        new HashSet<>(roadmap.getActivities()).forEach(roadmap::removeActivity);
        roadmapRepository.delete(roadmap);
    }

    @Transactional(readOnly = true)
    public Roadmap getRoadmapDetails(Long id, User loggedUser) {
        Roadmap roadmap = roadmapRepository.findByIdWithActivities(id);
        if (roadmap == null) {
            throw new EntityNotFoundException("Roadmap not found: " + id);
        }
        boolean isPublicApproved = roadmap.getVisibility() == RoadmapVisibility.PUBLIC && roadmap.getStatus() == ModerationStatus.APPROVED;
        boolean isOwner = loggedUser != null && roadmap.getOwner() != null && Objects.equals(roadmap.getOwner().getId(), loggedUser.getId());
        if (!isPublicApproved && !isOwner) {
            throw new SecurityException("Access denied to roadmap " + id);
        }
        return roadmap;
    }

    @Transactional
    public Roadmap addActivityToRoadmap(Long roadmapId, Long activityId, User loggedUser) {
        Roadmap roadmap = findByIdAndOwnerOrThrow(roadmapId, loggedUser);
        Activity activity = activityService.findByIdOrThrow(activityId);
        boolean isPublicOrSystem = activity.getType() == ActivityType.SYSTEM || (activity.getType() == ActivityType.CUSTOM_PUBLIC && activity.getModerationStatus() == ModerationStatus.APPROVED);
        boolean isOwn = activity.getCreator() != null && Objects.equals(activity.getCreator().getId(), loggedUser.getId());
        if (!isPublicOrSystem && !isOwn) {
            throw new SecurityException("Cannot add this activity to roadmap.");
        }
        roadmap.addActivity(activity);
        return roadmapRepository.save(roadmap);
    }

    @Transactional
    public Roadmap removeActivityFromRoadmap(Long roadmapId, Long activityId, User loggedUser) {
        Roadmap roadmap = findByIdAndOwnerOrThrow(roadmapId, loggedUser);
        Activity activity = activityService.findByIdOrThrow(activityId);
        if (!roadmap.getActivities().contains(activity)) {
            throw new IllegalArgumentException("Activity " + activityId + " does not belong to roadmap " + roadmapId);
        }
        roadmap.removeActivity(activity);
        return roadmapRepository.save(roadmap);
    }

    private Roadmap findByIdAndOwnerOrThrow(Long id, User loggedUser) {
        if (loggedUser == null || loggedUser.getId() == null) {
            throw new IllegalArgumentException("Invalid or not logged user.");
        }
        return roadmapRepository.findById(id)
                .filter(r -> r.getOwner() != null && r.getOwner().getId().equals(loggedUser.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Roadmap not found or does not belong to user: " + id));
    }

    @Transactional(readOnly = true)
    public Page<Roadmap> listPendingModeration(Pageable pageable) {
        return roadmapRepository.findByVisibilityAndStatus(RoadmapVisibility.PUBLIC, ModerationStatus.PENDING, pageable);
    }

    @Transactional
    public Roadmap moderateRoadmap(Long id, ModerationStatus newStatus, User admin) {
        Roadmap roadmap = roadmapRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Roadmap not found: " + id));
        if (roadmap.getVisibility() != RoadmapVisibility.PUBLIC) {
            throw new IllegalArgumentException("Only PUBLIC roadmaps can be moderated.");
        }
        if (newStatus == ModerationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot set status to PENDING manually.");
        }
        roadmap.setStatus(newStatus);
        return roadmapRepository.save(roadmap);
    }

    @Transactional(readOnly = true)
    public Page<Roadmap> listFavoriteRoadmaps(User loggedUser, Pageable pageable) {
        if (loggedUser == null || loggedUser.getId() == null) {
            throw new IllegalArgumentException("Invalid or not logged user.");
        }
        return roadmapRepository.findFavoritedByUserId(loggedUser.getId(), pageable);
    }

    @Transactional(readOnly = true)
    public Long countByOwnerId(Long userId) {
        return roadmapRepository.countByOwnerId(userId);
    }

}

