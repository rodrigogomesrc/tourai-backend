package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.exception.UnauthorizedException;
import br.imd.ufrn.tourai.model.Comment;
import br.imd.ufrn.tourai.model.NotificationType;
import br.imd.ufrn.tourai.model.Post;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.CommentRepository;
import br.imd.ufrn.tourai.repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    public CommentService(
            CommentRepository commentRepository,
            UserService userService,
            PostRepository postRepository,
            NotificationService notificationService) {

        this.commentRepository = commentRepository;
        this.userService = userService;
        this.postRepository = postRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public Comment addComment(Integer postId, Long commentatorId, String content) {
        Optional<User> commentator = userService.findById(commentatorId);
        if (commentator.isEmpty()) {
            throw new ResourceNotFoundException("User not found");
        }

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setCommentator(commentator.get());
        comment.setDate(Instant.now());
        comment.setContent(content);

        User postUser = post.getUser();
        if (!postUser.getId().equals(commentatorId)) {
            notificationService.create(
                    postUser, commentator.get(), NotificationType.COMMENT, content, Long.valueOf(post.getId()));
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Integer commentId, Long currentUserId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getCommentator().getId().equals(currentUserId)) {
            throw new UnauthorizedException("You can only delete your own comments");
        }

        commentRepository.delete(comment);
    }

    public List<Comment> getRecentComments(Integer postId, int quantity) {
        return commentRepository.findRecentByPost(postId, PageRequest.of(0, quantity));
    }


    public List<Comment> getOlderComments(Integer postId, Instant lastCommentDate, int quantity) {
        return commentRepository.findOlderByPost(postId, lastCommentDate, PageRequest.of(0, quantity));
    }
}
