package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.FavoriteRoadmap;
import br.imd.ufrn.tourai.model.Roadmap;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.FavoriteRoadmapRepository;
import br.imd.ufrn.tourai.repository.RoadmapRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavoriteRoadmapService {

    private final FavoriteRoadmapRepository favoriteRoadmapRepository;
    private final RoadmapRepository roteiroRepository;
    private final UserService userService;

    public FavoriteRoadmapService(FavoriteRoadmapRepository favoriteRoadmapRepository,
                                  RoadmapRepository roteiroRepository,
                                  UserService userService) {
        this.favoriteRoadmapRepository = favoriteRoadmapRepository;
        this.roteiroRepository = roteiroRepository;
        this.userService = userService;
    }

    public void favoriteRoadmap(Long roadmapId, Long userId) {

        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Optional<Roadmap> roadmap = roteiroRepository.findById(roadmapId);
        if (roadmap.isEmpty()) {
            throw new ResourceNotFoundException("Roadmap not found with id: " + roadmapId);
        }

        FavoriteRoadmap favoriteRoadmap = new FavoriteRoadmap();
        favoriteRoadmap.setUser(user.get());
        favoriteRoadmap.setRoadmap(roadmap.get());
        favoriteRoadmap.getRoadmap().setId(roadmapId);
        favoriteRoadmapRepository.save(favoriteRoadmap);

    }

    public void unfavoriteRoadmap(Long roadmapId, Long userId) {
        Optional<FavoriteRoadmap> favoriteRoadmap = favoriteRoadmapRepository.findByRoadmapIdAndUserId(roadmapId, userId);
        if (favoriteRoadmap.isEmpty()) {
            throw new ResourceNotFoundException("Favorite roadmap not found for roadmap id: " + roadmapId + " and user id: " + userId);
        }
        favoriteRoadmapRepository.delete(favoriteRoadmap.get());
    }

    public List<FavoriteRoadmap> findByUserId(Long userId) {

        Optional<User> user = userService.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        return favoriteRoadmapRepository.findByUserId(userId);
    }

}
