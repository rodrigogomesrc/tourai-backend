package br.imd.ufrn.tourai.repository;

import br.imd.ufrn.tourai.model.Atividade;
import br.imd.ufrn.tourai.model.StatusModeracao;
import br.imd.ufrn.tourai.model.TipoAtividade;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface AtividadeRepository extends JpaRepository<Atividade, Long> {

    //T12-Catálogo-Atividades – lista com busca e filtros por tags/tema.
    //Busca atividades cadastradas pelo sistema ou as atividades do público
    //aprovadas por nome
    @Query("SELECT a FROM Atividade a WHERE " +
            "(a.tipo = :tipoSistema OR " +
            "(a.tipo = :tipoPublica AND a.statusModeracao = :statusAprovada)) " +
            "AND LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<Atividade> findPublicasByNome(
            @Param("tipo_sistema")TipoAtividade tipoSistema,
            @Param("tipo_publica") TipoAtividade tipoPublica,
            @Param("status_aprovada")StatusModeracao statusAprovada,
            @Param("nome") String nome,
            Pageable pageable);

    //T12-Catálogo-Atividades – lista com busca e filtros por tags/tema.
    //Busca atividades cadastradas pelo sistema ou as atividades do público
    //aprovadas por nome e tags
    @Query("SELECT DISTINCT a FROM Atividade a JOIN a.tags t WHERE " +
            "(a.tipo = :tipoSistema OR " +
            "(a.tipo = :tipoPublica AND a.statusModeracao = :statusAprovada)) " +
            "AND LOWER(a.nome) LIKE LOWER(CONCAT('%', :nome, '%')) " +
            "AND t IN :tags")
    Page<Atividade> findByNomeAndTags(
            @Param("tipoSistema") TipoAtividade tipoSistema,
            @Param("tipoPublica") TipoAtividade tipoPublica,
            @Param("statusAprovada") StatusModeracao statusAprovada,
            @Param("nome") String nome,
            @Param("tags") Collection<String> tags,
            Pageable pageable);

    List<Atividade> findByCriadorId(Long criadorId);

    //T26-Fila-Moderação – cards com conteúdo pendente (atividade/roteiro), ações (aprovar/reprovar/editar tags), histórico.
    List<Atividade> findByTipoAndStatusModerao(TipoAtividade tipo, StatusModeracao status);
}
