package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.service.FollowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> follow(@PathVariable Long id, @RequestParam Long followerId) {
        followService.followUser(followerId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/unfollow")
    public ResponseEntity<Void> unfollow(@PathVariable Long id, @RequestParam Long followerId) {
        followService.unfollowUser(followerId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/follow-stats")
    public ResponseEntity<Map<String, Object>> getStats(@PathVariable Long id, @RequestParam(required = false) Long currentUserId) {
        return ResponseEntity.ok(followService.getFollowStats(id, currentUserId));
    }
}