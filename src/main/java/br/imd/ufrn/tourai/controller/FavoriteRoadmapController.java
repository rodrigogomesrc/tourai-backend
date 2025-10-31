package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.FavoriteRoadmap;
import br.imd.ufrn.tourai.service.FavoriteRoadmapService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorites/roadmaps")
public class FavoriteRoadmapController {

    private final FavoriteRoadmapService favoriteRoadmapService;

    public FavoriteRoadmapController(FavoriteRoadmapService favoriteRoadmapService) {
        this.favoriteRoadmapService = favoriteRoadmapService;
    }

    @PostMapping("/{roadmapId}")
    public ResponseEntity<Void> favoriteRoadmap(@PathVariable Long roadmapId, @RequestParam Long userId) {

        try {
            favoriteRoadmapService.favoriteRoadmap(roadmapId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{roadmapId}")
    public ResponseEntity<Void> unfavoriteRoadmap(
            @PathVariable Long roadmapId,
            @RequestParam Long userId) {

        try {
            favoriteRoadmapService.unfavoriteRoadmap(roadmapId, userId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteRoadmap>> getUserFavorites(@PathVariable Long userId) {
        try {
            List<FavoriteRoadmap> favorites = favoriteRoadmapService.findByUserId(userId);
            return ResponseEntity.ok(favorites);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}