package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.model.ModerationStatus;
import br.imd.ufrn.tourai.model.Roadmap;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.service.RoadmapService;
import br.imd.ufrn.tourai.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;
    private final UserService userService;

    public RoadmapController(RoadmapService roadmapService, UserService userService) {
        this.roadmapService = roadmapService;
        this.userService = userService;
    }

    @GetMapping("/mine")
    public ResponseEntity<?> listMine(@RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            return ResponseEntity.ok(roadmapService.listMyRoadmaps(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Roadmap roadmap, @RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            Roadmap created = roadmapService.createRoadmap(roadmap, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> details(@PathVariable Long id, @RequestParam(required = false) Long userId) {
        try {
            User user = userId != null ? userService.findByIdOrThrow(userId) : null;
            Roadmap r = roadmapService.getRoadmapDetails(id, user);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Roadmap update, @RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            Roadmap updated = roadmapService.updateRoadmap(id, update, user);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, @RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            roadmapService.deleteRoadmap(id, user);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/{roadmapId}/activities/{activityId}")
    public ResponseEntity<?> addActivity(@PathVariable Long roadmapId, @PathVariable Long activityId, @RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            Roadmap r = roadmapService.addActivityToRoadmap(roadmapId, activityId, user);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @DeleteMapping("/{roadmapId}/activities/{activityId}")
    public ResponseEntity<?> removeActivity(@PathVariable Long roadmapId, @PathVariable Long activityId, @RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            Roadmap r = roadmapService.removeActivityFromRoadmap(roadmapId, activityId, user);
            return ResponseEntity.ok(r);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/moderation/pending")
    public ResponseEntity<List<Roadmap>> pendingModeration() {
        return ResponseEntity.ok(roadmapService.listPendingModeration());
    }

    @PutMapping("/{id}/moderation")
    public ResponseEntity<?> moderate(@PathVariable Long id, @RequestParam("status") ModerationStatus status, @RequestParam Long userId) {
        try {
            User admin = userService.findByIdOrThrow(userId); // future: check admin privileges
            Roadmap moderated = roadmapService.moderateRoadmap(id, status, admin);
            return ResponseEntity.ok(moderated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/favorites")
    public ResponseEntity<?> listFavorites(@RequestParam Long userId) {
        try {
            User user = userService.findByIdOrThrow(userId);
            return ResponseEntity.ok(roadmapService.listFavoriteRoadmaps(user));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
