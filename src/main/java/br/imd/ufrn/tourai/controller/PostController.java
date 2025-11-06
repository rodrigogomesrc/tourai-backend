package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.exception.ConflictException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.Post;
import br.imd.ufrn.tourai.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post, @RequestParam Integer userId) {
        Post salvo = postService.save(post, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> findById(@PathVariable Integer id) {
        Post post = postService.findById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(post);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        Post post = postService.findById(id);
        if (post == null) {
            return ResponseEntity.notFound().build();
        }
        postService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/new")
    public ResponseEntity<List<Post>> listNewer(@RequestParam(defaultValue = "10") int quantity) {
        List<Post> posts = postService.getRecentPosts(quantity);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/older")
    public ResponseEntity<List<Post>> listOlder(
            @RequestParam Instant lastPostDate,
            @RequestParam(defaultValue = "10") int quantity) {
        List<Post> posts = postService.getOlderPosts(lastPostDate, quantity);
        return ResponseEntity.ok(posts);
    }


    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> addLike(@PathVariable Integer postId, @RequestParam Integer userId) {

        try {
            postService.addLike(postId, Long.valueOf(userId));
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{postId}/likes")
    public ResponseEntity<Void> removeLike(
            @PathVariable Integer postId,
            @RequestParam Integer userId) {

        try {
            postService.removeLike(postId, Long.valueOf(userId));
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{postId}/liked")
    public ResponseEntity<Boolean> hasUserLiked(
            @PathVariable Integer postId,
            @RequestParam Integer userId) {
        boolean liked = postService.hasUserLiked(postId, Long.valueOf(userId));
        return ResponseEntity.ok(liked);
    }

}
