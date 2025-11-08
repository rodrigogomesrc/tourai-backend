package br.imd.ufrn.tourai.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.imd.ufrn.tourai.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);
}
