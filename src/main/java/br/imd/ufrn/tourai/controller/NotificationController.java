package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.model.Notification;
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
            @RequestParam(defaultValue = "10") int quantity) {

        List<Notification> notifications = notificationService.getRecentNotifications(userId, quantity);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> listUnread(@RequestParam Integer userId) {
        List<Notification> notifications = notificationService.getUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Integer id) {
        notificationService.markAsReceived(id);
        return ResponseEntity.noContent().build();
    }
}