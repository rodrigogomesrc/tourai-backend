package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.exception.UnauthorizedException;
import br.imd.ufrn.tourai.model.Comment;
import br.imd.ufrn.tourai.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/comments")
public class CommentsController {

    private final CommentService commentService;

    public CommentsController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{postId}/new")
    public ResponseEntity<List<Comment>> listNewerComments(
            @PathVariable Integer postId,
            @RequestParam(defaultValue = "10") int quantity) {

        try {
            List<Comment> comments = commentService.getRecentComments(postId, quantity);
            return ResponseEntity.ok(comments);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{postId}/older")
    public ResponseEntity<List<Comment>> listOlderComments(
            @PathVariable Integer postId,
            @RequestParam Instant lastCommentDate,
            @RequestParam(defaultValue = "10") int quantity) {

        try {
            List<Comment> comments = commentService.getOlderComments(postId, lastCommentDate, quantity);
            return ResponseEntity.ok(comments);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{postId}")
    public ResponseEntity<Comment> addComment(
            @PathVariable Integer postId,
            @RequestParam Integer userId,
            @RequestBody String content) {


        try {
            Comment comment = commentService.addComment(postId, Long.valueOf(userId), content);
            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Integer commentId,
            @RequestParam Integer userId) {

        try {
            commentService.deleteComment(commentId, Long.valueOf(userId));
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (UnauthorizedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
