package br.imd.ufrn.tourai.service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.imd.ufrn.tourai.dto.UserRequest;
import br.imd.ufrn.tourai.exception.BadRequestException;
import br.imd.ufrn.tourai.exception.ConflictException;
import br.imd.ufrn.tourai.exception.ResourceNotFoundException;
import br.imd.ufrn.tourai.model.User;
import br.imd.ufrn.tourai.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User create(UserRequest userReq) {
        if (userReq == null) throw new BadRequestException("Requisição de usuário é obrigatória");
        if (userReq.getEmail() == null || userReq.getEmail().isBlank()) throw new BadRequestException("Email é obrigatório");

        Optional<User> exists = userRepository.findByEmail(userReq.getEmail());
        if (exists.isPresent()) {
            throw new ConflictException("Email já cadastrado");
        }

    String password = userReq.getPassword() == null ? "" : userReq.getPassword();
    String encoded = passwordEncoder.encode(password);
    User user = new User(userReq.getName(), userReq.getEmail(), encoded,
        userReq.getProfilePhotoUrl(), userReq.getBio(), userReq.getInterests());
        User saved = userRepository.save(user);
        return saved;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User update(Long id, UserRequest userReq) {
        if (id == null) throw new BadRequestException("id é obrigatório");
        if (userReq == null) throw new BadRequestException("Requisição de usuário é obrigatória");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));

    if (userReq.getName() != null) user.setName(userReq.getName());

        if (userReq.getEmail() != null && !userReq.getEmail().isBlank()) {
            Optional<User> other = userRepository.findByEmail(userReq.getEmail());
            if (other.isPresent() && !Objects.equals(other.get().getId(), id)) {
                throw new ConflictException("Email já cadastrado por outro usuário");
            }
            user.setEmail(userReq.getEmail());
        }

        if (userReq.getPassword() != null && !userReq.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(userReq.getPassword()));
        }

        if (userReq.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(userReq.getProfilePhotoUrl());
        }

        if (userReq.getBio() != null) {
            user.setBio(userReq.getBio());
        }

        if (userReq.getInterests() != null) {
            user.setInterests(userReq.getInterests());
        }

        User saved = userRepository.save(user);
        return saved;
    }

    public void delete(Long id) {
        if (id == null) throw new BadRequestException("id é obrigatório");
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário não encontrado com id: " + id);
        }
        userRepository.deleteById(id);
    }

    public boolean authenticate(String email, String password) {
        if (email == null || email.isBlank()) throw new BadRequestException("Email é obrigatório para autenticação");
        if (password == null) throw new BadRequestException("Senha é obrigatória para autenticação");

        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) return false;
        return passwordEncoder.matches(password, user.get().getPassword());
    }

    public User findByIdOrThrow(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id: " + id));
    }
}
