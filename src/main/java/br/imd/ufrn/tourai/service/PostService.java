package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.exception.ConflictException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.exception.UnauthorizedException;
import br.imd.ufrn.tourai.model.Comment;
import br.imd.ufrn.tourai.model.CommentLike;
import br.imd.ufrn.tourai.model.Post;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.CommentRepository;
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
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final UserService userService;

    public PostService(PostRepository postRepository,
                       CommentRepository commentRepository,
                       LikeRepository likeRepository,
                       UserService userService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.likeRepository = likeRepository;
        this.userService = userService;
    }

    public Post save(Post post) {
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

    public List<Post> getRecentPosts(int quantity) {
        return postRepository.findLast(PageRequest.of(0, quantity));
    }

    public List<Post> getOlderPosts(Instant lastPostDate, int quantity) {
        return postRepository.findOlder(lastPostDate, PageRequest.of(0, quantity));
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

        CommentLike commentLike = new CommentLike();
        commentLike.setPost(post);
        commentLike.setLiker(liker.get());
        commentLike.setDate(Instant.now());
        likeRepository.save(commentLike);
    }

    @Transactional
    public void removeLike(Integer postId, Long likerId) {
        CommentLike commentLike = likeRepository
                .findByPostIdAndLikerId(postId, Math.toIntExact(likerId))
                .orElseThrow(() -> new ResourceNotFoundException("Like not found"));

        likeRepository.delete(commentLike);
    }


}
