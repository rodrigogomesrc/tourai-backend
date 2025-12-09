package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.model.Notification;
import br.imd.ufrn.tourai.model.NotificationType;
import br.imd.ufrn.tourai.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> listRecent(
            @RequestParam Integer userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) NotificationType type) {

        List<Notification> notifications = notificationService.getRecentNotifications(userId, page, size, type);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> listUnread(
            @RequestParam Integer userId,
            @RequestParam(required = false) NotificationType type) {

        List<Notification> notifications = notificationService.getUnreadNotifications(userId, type);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsReceived(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/completed")
    public ResponseEntity<Void> markActionCompleted(@PathVariable Integer id) {
        notificationService.markActionAsCompleted(id);
        return ResponseEntity.noContent().build();
    }
}