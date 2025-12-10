package br.imd.ufrn.tourai.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.imd.ufrn.tourai.config.CustomUserDetails;
import br.imd.ufrn.tourai.dto.AuthResponse;
import br.imd.ufrn.tourai.dto.LoginRequest;
import br.imd.ufrn.tourai.dto.UserRequest;
import br.imd.ufrn.tourai.dto.UserResponse;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.exception.UnauthorizedException;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.service.AuthenticationJwtService;
import br.imd.ufrn.tourai.service.ItineraryService;
import br.imd.ufrn.tourai.service.PostService;
import br.imd.ufrn.tourai.service.RoadmapService;
import br.imd.ufrn.tourai.service.UserService;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoadmapService roadmapService;
    private final ItineraryService itineraryService;
    private final PostService postService;

    private final AuthenticationManager authenticationManager;
    private final AuthenticationJwtService jwtService;

    public UserController(UserService userService, RoadmapService roadmapService, ItineraryService itineraryService, PostService postService, AuthenticationManager authenticationManager, AuthenticationJwtService jwtService) {
        this.userService = userService;
        this.roadmapService = roadmapService;
        this.itineraryService = itineraryService;
        this.postService = postService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest requestUser) {
        User user = userService.create(requestUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(user));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> get(@PathVariable Long id) {
       Optional<User> userOpt = userService.findById(id);

        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(toResponse(userOpt.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @RequestBody UserRequest requestUser) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        Long loggedId = userDetails.getId();

        if (!loggedId.equals(id)) {
            throw new UnauthorizedException("Você não tem permissão para alterar este perfil.");
        }

        User user = userService.update(id, requestUser);
        return ResponseEntity.ok(toResponse(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        var userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles("USER")
                .build();

        String accessToken = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(accessToken, toResponse(user)));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getProfilePhotoUrl(), user.getBio(), user.getInterests());
    }

    @GetMapping("/{id}/stats")
    public ResponseEntity<Map<String, Long>> getUserStats(@PathVariable Long id) {
        userService.findByIdOrThrow(id);

        Long totalRoadmaps = roadmapService.countByOwnerId(id);
        Long totalItineraries = itineraryService.countByUserId(id);
        Long totalPosts = postService.countByUserId(id);

        Map<String, Long> stats = Map.of(
            "roteiros", totalRoadmaps,
            "itinerarios", totalItineraries,
            "postagens", totalPosts
        );

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> search(@RequestParam("query") String query) {
        List<User> users = userService.searchByName(query);

        List<UserResponse> response = users.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
