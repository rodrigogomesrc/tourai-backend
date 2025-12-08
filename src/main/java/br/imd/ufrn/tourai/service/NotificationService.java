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

    public Notification create(User destination, User source, NotificationType type){
        Notification newNotification = new Notification();
        newNotification.setDestination(destination);
        newNotification.setSource(source);
        newNotification.setType(type);
        newNotification.setCreatedAt(Instant.now());
        return this.notificationRepository.save(newNotification);
    }

    public List<Notification> getRecentNotifications(Integer userId, int quantity) {
        userService.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findRecent(userId, PageRequest.of(0, quantity));
    }

    public List<Notification> getUnreadNotifications(Integer userId) {

        userService.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.findUnread(userId);
    }

    public void markAsReceived(Integer notificationId) {

      notificationRepository.findById(Long.valueOf(notificationId))
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

      notificationRepository.markAsReceived(notificationId);
    }

}
