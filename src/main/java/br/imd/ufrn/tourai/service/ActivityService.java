package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.model.*;
import br.imd.ufrn.tourai.repository.ActivityRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Transactional(readOnly = true)
    public Page<Activity> searchPublicActivities(String searchTerm, Collection<String> tags, Pageable pageable) {
        String term = StringUtils.hasText(searchTerm) ? searchTerm : "";
        if (tags != null && !tags.isEmpty()) {
            return activityRepository.findPublicByNameAndTags(ActivityType.SYSTEM, ActivityType.CUSTOM_PUBLIC, ModerationStatus.APPROVED, term, tags, pageable);
        } else {
            return activityRepository.findPublicByName(ActivityType.SYSTEM, ActivityType.CUSTOM_PUBLIC, ModerationStatus.APPROVED, term, pageable);
        }
    }

    @Transactional(readOnly = true)
    public Activity getPublicOrOwnActivity(Long id, User loggedUser) {
        Activity activity = findByIdOrThrow(id);
        boolean isApprovedPublic = activity.getType() == ActivityType.SYSTEM ||
                (activity.getType() == ActivityType.CUSTOM_PUBLIC && activity.getModerationStatus() == ModerationStatus.APPROVED);
        boolean isOwn = loggedUser != null && activity.getCreator() != null && Objects.equals(activity.getCreator().getId(), loggedUser.getId());
        if (isApprovedPublic || isOwn) {
            return activity;
        }
        throw new SecurityException("User not allowed to view this activity.");
    }

    @Transactional
    public Activity createCustomActivity(Activity newActivity, User loggedUser) {
        if (loggedUser == null) {
            throw new IllegalArgumentException("User must be logged in to create an activity.");
        }
        if (newActivity.getType() == ActivityType.SYSTEM) {
            throw new IllegalArgumentException("Cannot create SYSTEM activity.");
        }
        if (!StringUtils.hasText(newActivity.getName())) {
            throw new IllegalArgumentException("Activity name cannot be empty.");
        }
        Activity activity = new Activity();
        activity.setName(newActivity.getName());
        activity.setDescription(newActivity.getDescription());
        activity.setLocation(newActivity.getLocation());
        activity.setMediaUrl(newActivity.getMediaUrl());
        if (newActivity.getTags() != null) {
            activity.setTags(new HashSet<>(newActivity.getTags()));
        }
        activity.setCreator(loggedUser);
        
        if(activity.getType() == null){
            activity.setType(ActivityType.SYSTEM);
        }else{
            activity.setType(newActivity.getType());
        }

        if (activity.getType() == ActivityType.CUSTOM_PUBLIC) {
            activity.setModerationStatus(ModerationStatus.PENDING);
        } else {
            activity.setModerationStatus(null);
        }
        return activityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public Activity findByIdOrThrow(Long id) {
        return activityRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Activity not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Activity> listPendingModeration() {
        return activityRepository.findByTypeAndModerationStatus(ActivityType.CUSTOM_PUBLIC, ModerationStatus.PENDING);
    }

    @Transactional
    public Activity moderateActivity(Long id, ModerationStatus newStatus, User admin) {
        Activity activity = findByIdOrThrow(id);
        if (activity.getType() != ActivityType.CUSTOM_PUBLIC) {
            throw new IllegalArgumentException("Only CUSTOM_PUBLIC activities can be moderated.");
        }
        if (newStatus == ModerationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot set status back to PENDING manually.");
        }
        activity.setModerationStatus(newStatus);
        return activityRepository.save(activity);
    }
}

