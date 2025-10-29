package br.imd.ufrn.tourai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.imd.ufrn.tourai.model.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
