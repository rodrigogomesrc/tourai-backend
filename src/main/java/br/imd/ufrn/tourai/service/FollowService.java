package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.exception.BadRequestException;
import br.imd.ufrn.tourai.exception.ConflictException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.Follow;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.FollowRepository;
import br.imd.ufrn.tourai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowService(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new BadRequestException("Você não pode seguir a si mesmo.");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(followerId, followingId)) {
            throw new ConflictException("Você já segue este usuário.");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário seguidor não encontrado."));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário a ser seguido não encontrado."));

        Follow follow = new Follow(follower, following);
        followRepository.save(follow);
    }

    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollowerIdAndFollowingId(followerId, followingId)
                .orElseThrow(() -> new ResourceNotFoundException("Você não segue este usuário."));

        followRepository.delete(follow);
    }

    public Map<String, Object> getFollowStats(Long userId, Long currentUserId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado.");
        }

        Long followersCount = followRepository.countByFollowingId(userId);
        Long followingCount = followRepository.countByFollowerId(userId);

        boolean isFollowing = false;
        if (currentUserId != null) {
            isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, userId);
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("followersCount", followersCount);
        stats.put("followingCount", followingCount);
        stats.put("isFollowing", isFollowing);

        return stats;
    }
}