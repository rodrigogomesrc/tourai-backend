package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.model.Activity;
import br.imd.ufrn.tourai.model.ModerationStatus;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.service.ActivityService;
import br.imd.ufrn.tourai.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    private final ActivityService activityService;
    private final UserService userService;

    public ActivityController(ActivityService activityService, UserService userService) {
        this.activityService = activityService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<Activity>> listPublic(
            @RequestParam(name = "search", required = false) String search,
            @RequestParam(name = "tags", required = false) Collection<String> tags,
            Pageable pageable) {
        Page<Activity> page = activityService.searchPublicActivities(search, tags, pageable);
        return ResponseEntity.ok(page);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDetails(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            User user = userId != null ? userService.findByIdOrThrow(userId) : null;
            Activity activity = activityService.getPublicOrOwnActivity(id, user);
            return ResponseEntity.ok(activity);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Activity activity, @RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            Activity created = activityService.createCustomActivity(activity, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/moderation/pending")
    public ResponseEntity<Page<Activity>> listPendingModeration(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(activityService.listPendingModeration(pageable));
    }

    @PutMapping("/{id}/moderation")
    public ResponseEntity<?> moderate(@PathVariable Long id, @RequestParam("status") ModerationStatus status, @RequestParam Long userId) {
        try {
            User admin = userService.findByIdOrThrow(userId);
            Activity moderated = activityService.moderateActivity(id, status, admin);
            return ResponseEntity.ok(moderated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

