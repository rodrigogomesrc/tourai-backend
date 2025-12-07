package br.imd.ufrn.tourai.service;

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

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;

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
        return notificationRepository.findRecent(userId, PageRequest.of(0, quantity));
    }

    public List<Notification> getUnreadNotifications(Integer userId) {
        return notificationRepository.findUnread(userId);
    }

    public void markAsReceived(Integer notificationId) {
        notificationRepository.markAsReceived(notificationId);
    }

}
