package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.Notification;
import br.imd.ufrn.tourai.model.NotificationType;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;

    public NotificationService(NotificationRepository notificationRepository, UserService userService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
    }

    public Notification create(User destination,
                               User source,
                               NotificationType type,
                               String payload,
                               Long entityId) {

        Notification newNotification = new Notification();
        newNotification.setDestination(destination);
        newNotification.setSource(source);
        newNotification.setType(type);
        newNotification.setCreatedAt(Instant.now());
        newNotification.setPayload(payload);
        newNotification.setEntityId(entityId);
        newNotification.setReceived(false);
        return this.notificationRepository.save(newNotification);
    }

    public List<Notification> getRecentNotifications(Integer userId, int page, int size, NotificationType type) {
        userService.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findRecent(userId, type, PageRequest.of(page, size));
    }

    public List<Notification> getUnreadNotifications(Integer userId, NotificationType type) {

        userService.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findUnread(userId, type);
    }

    public void markAsReceived(Integer notificationId) {
        notificationRepository.findById(Long.valueOf(notificationId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.markAsReceived(notificationId);
    }

    public void markActionAsCompleted(Integer notificationId) {
        notificationRepository.findById(Long.valueOf(notificationId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.markActionAsCompleted(notificationId);
    }

}