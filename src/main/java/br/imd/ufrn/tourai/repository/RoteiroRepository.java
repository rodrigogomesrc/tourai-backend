package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Roteiro;
import br.imd.ufrn.tourai.model.StatusModeracao;
import br.imd.ufrn.tourai.model.VisibilidadeRoteiro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoteiroRepository extends JpaRepository<Roteiro, Long> {

    //T8-Meus-Roteiros – listagem (rascunho, privado, compartilhado, público).
    List<Roteiro> findByUserIdOrderByTituloAsc(Long userId);

    //T5-Explorar – busca e filtros (destino, tema, duração, avaliação); cards de roteiros/atividades públicos
    //T26-Fila-Moderação – cards com conteúdo pendente (atividade/roteiro), ações (aprovar/reprovar/editar tags), histórico.
    List<Roteiro> findByVisibilidadeAndStatus(VisibilidadeRoteiro visibilidade, StatusModeracao status);

    //Busca otimizada de um Roteiro com suas atividades
    @Query("SELECT r FROM Roteiro r LEFT JOIN FETCH r.atividades WHERE r.id = :id")
    Roteiro findByIdWithAtividades(Long id);

}
