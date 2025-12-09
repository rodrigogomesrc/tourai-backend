package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ai")
public class AIController {

    private final GeminiService geminiService;

    public AIController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<String> getRecommendations(@RequestParam Long userId) {
        return ResponseEntity.ok(geminiService.getRecommendations(userId));
    }
}
