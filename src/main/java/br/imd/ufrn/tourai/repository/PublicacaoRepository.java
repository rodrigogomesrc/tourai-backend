package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Publicacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PublicacaoRepository extends JpaRepository<Publicacao, Integer> {

    @Query("SELECT p FROM Publicacao p ORDER BY p.dataPublicacao DESC")
    List<Publicacao> findUltimas(Pageable pageable);

    @Query("SELECT p FROM Publicacao p WHERE p.dataPublicacao < :dataUltimo ORDER BY p.dataPublicacao DESC")
    List<Publicacao> findMaisAntigas(@Param("dataUltimo") Instant dataUltimo, Pageable pageable);

}
