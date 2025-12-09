package br.imd.ufrn.tourai.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import br.imd.ufrn.tourai.dto.gemini.GeminiRequest;
import br.imd.ufrn.tourai.dto.gemini.GeminiResponse;
import br.imd.ufrn.tourai.model.Activity;
import br.imd.ufrn.tourai.model.ActivityType;
import br.imd.ufrn.tourai.model.ModerationStatus;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.ActivityRepository;
import br.imd.ufrn.tourai.repository.UserRepository;

@Service
public class GeminiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.url}")
    private String apiUrl;

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final RestTemplate restTemplate;

    // TAGS FIXAS
    private static final List<String> TAGS_PERMITIDAS = List.of(
        "Praia", "Montanha", "Cidade", "Natureza", "Aventura",
        "Cultura", "Gastronomia", "História", "Arte", "Relaxamento",
        "Esportes", "Família", "Romântico", "Econômico", "Luxo"
    );

    // Armazena o cache de recomendações por usuário
    private final Map<Long, CachedRecommendation> recommendationCache = new ConcurrentHashMap<>();

    private static final Duration CACHE_DURATION = Duration.ofHours(12);

    // rate limiting + fila de timestamps das requisições
    private final Queue<Instant> requestTimestamps = new ConcurrentLinkedQueue<>();
    private static final int MAX_REQUESTS_PER_MINUTE = 4;

    public GeminiService(UserRepository userRepository, ActivityRepository activityRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.restTemplate = new RestTemplate();
    }

    public String getRecommendations(Long userId) {

        if (recommendationCache.containsKey(userId)) {
            CachedRecommendation cached = recommendationCache.get(userId);
            long hoursSince = Duration.between(cached.timestamp, Instant.now()).toHours();

            if (hoursSince < 12) {
                return cached.jsonResponse;
            }
        }

        synchronized (this) {
            cleanOldRequests();
            if (requestTimestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                if (recommendationCache.containsKey(userId)) {
                    return recommendationCache.get(userId).jsonResponse;
                }
                return "[]";
            }
            requestTimestamps.add(Instant.now());
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Set<String> userInterests = user.getInterests();
        String interestsString = userInterests.isEmpty()
            ? "pontos turísticos gerais"
            : String.join(", ", userInterests);

        List<Activity> availableActivities = activityRepository.findPublicByNameAndTags(
                ActivityType.SYSTEM,
                ActivityType.CUSTOM_PUBLIC,
                ModerationStatus.APPROVED,
                "",
                userInterests,
                PageRequest.of(0, 20)
        ).getContent();

        if (availableActivities.isEmpty()) {
            availableActivities = activityRepository.findPublicByName(
                ActivityType.SYSTEM, ActivityType.CUSTOM_PUBLIC, ModerationStatus.APPROVED, "", PageRequest.of(0, 10)
            ).getContent();
        }

        StringBuilder activitiesContext = new StringBuilder();
        for (Activity a : availableActivities) {
            activitiesContext.append(String.format("- ID: %d, Nome: %s, Descrição: %s, Local: %s\n",
                a.getId(), a.getName(), a.getDescription(), a.getLocation()));
        }

        String tagsPermitidasTexto = String.join(", ", TAGS_PERMITIDAS);

        String prompt = String.format(
            "Atue como um guia turístico. O usuário tem interesse em: %s. " +
            "Crie 3 sugestões de roteiros baseados nas atividades abaixo.\n\n" +

            "REGRAS OBRIGATÓRIAS:\n" +
            "1. Use APENAS atividades desta lista para compor o roteiro:\n%s\n" +
            "2. Para as 'tags' do roteiro, escolha APENAS valores desta lista: [%s].\n" +
            "3. O campo 'description' DEVE ter no MÁXIMO 255 caracteres. Seja conciso e direto.\n\n" +

            "Responda APENAS com um Array JSON puro (sem markdown), onde cada objeto tem: " +
            "'title', 'description' (max 255 chars), 'tags' (array) e 'activities' (array de strings com os NOMES EXATOS).",

            interestsString,
            activitiesContext.toString(),
            tagsPermitidasTexto
        );

        try {
            GeminiRequest request = new GeminiRequest(List.of(
                new GeminiRequest.Content(List.of(new GeminiRequest.Part(prompt)))
            ));

            String url = apiUrl + "?key=" + apiKey;
            GeminiResponse response = restTemplate.postForObject(url, request, GeminiResponse.class);

            if (response != null && !response.candidates().isEmpty()) {
                String resultText = response.candidates().get(0).content().parts().get(0).text();

                String cleanJson = resultText.replaceAll("```json", "").replaceAll("```", "").trim();

                recommendationCache.put(userId, new CachedRecommendation(cleanJson, Instant.now()));

                return cleanJson;
            }
        } catch (HttpClientErrorException e) {
            System.err.println("ERRO NA API GEMINI: " + e.getStatusCode());
            System.err.println("RESPOSTA DO GOOGLE: " + e.getResponseBodyAsString());
            throw new RuntimeException("Gemini está indisponível no momento.");

        } catch (Exception e) {
            e.printStackTrace();
            if (recommendationCache.containsKey(userId)) {
                return recommendationCache.get(userId).jsonResponse;
            }
        }

        return "[]";
    }

    private void cleanOldRequests() {
        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        while (!requestTimestamps.isEmpty() && requestTimestamps.peek().isBefore(oneMinuteAgo)) {
            requestTimestamps.poll();
        }
    }

    private static class CachedRecommendation {
        String jsonResponse;
        Instant timestamp;

        public CachedRecommendation(String jsonResponse, Instant timestamp) {
            this.jsonResponse = jsonResponse;
            this.timestamp = timestamp;
        }
    }
}
