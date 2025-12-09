package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.exception.ConflictException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.NotificationType;
import br.imd.ufrn.tourai.model.PostLike;
import br.imd.ufrn.tourai.model.Post;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.LikeRepository;
import br.imd.ufrn.tourai.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;
    private final NotificationService notificationService;

    public PostService(PostRepository postRepository,
                       LikeRepository likeRepository,
                       UserService userService,
                       NotificationService notificationService) {
        this.postRepository = postRepository;
        this.likeRepository = likeRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    public Post save(Post post, Integer userId) {
        User user = userService.findById(Long.valueOf(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        post.setUser(user);
        Instant now = Instant.now();
        post.setPostDate(now);
        return postRepository.save(post);
    }

    public Post findById(Integer id) {
        return postRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        postRepository.deleteById(id);
    }

    public List<Post> getRecentPosts(int quantity, String search) {
        return postRepository.findLast(search, PageRequest.of(0, quantity));
    }

    public List<Post> getOlderPosts(Instant lastPostDate, int quantity, String search) {
        return postRepository.findOlder(lastPostDate, search, PageRequest.of(0, quantity));
    }

    @Transactional
    public void addLike(Integer postId, Long likerId) {
        Optional<User> liker = userService.findById(likerId);
        if (liker.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean alreadyLiked = likeRepository
                .findByPostIdAndLikerId(postId, Math.toIntExact(likerId))
                .isPresent();

        if (alreadyLiked) {
            throw new ConflictException("User already liked this post");
        }

        User postUser = post.getUser();
        if (!postUser.getId().equals(likerId)) {
            notificationService.create(
                    postUser, liker.get(), NotificationType.LIKE, "", Long.valueOf(post.getId()));
        }

        PostLike postLike = new PostLike();
        postLike.setPost(post);
        postLike.setLiker(liker.get());
        postLike.setDate(Instant.now());
        likeRepository.save(postLike);
    }

    @Transactional
    public void removeLike(Integer postId, Long likerId) {
        PostLike postLike = likeRepository
                .findByPostIdAndLikerId(postId, Math.toIntExact(likerId))
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        likeRepository.delete(postLike);
    }

    public boolean hasUserLiked(Integer postId, Long userId) {
        return likeRepository.findByPostIdAndLikerId(postId, Math.toIntExact(userId)).isPresent();
    }

    public Long countByUserId(Long userId) {
        return postRepository.countByUserId(userId);
    }
}
