package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.model.Atividade;
import br.imd.ufrn.tourai.model.StatusModeracao;
import br.imd.ufrn.tourai.model.TipoAtividade;
import br.imd.ufrn.tourai.repository.AtividadeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

@Service
public class AtividadeService {

    @Autowired
    private AtividadeRepository atividadeRepository;

    //T12-Catálogo-Atividades – lista com busca e filtros por tags/tema.
    @Transactional(readOnly = true)
    public Page<Atividade> buscarAtividadesPublicas(String termoBusca, Collection<String> tags, Pageable pageable) {
        String termo = StringUtils.hasText(termoBusca) ? termoBusca : ""; // Garante que não é nulo

        if (tags != null && !tags.isEmpty()) {
            return atividadeRepository.findByNomeAndTags(
                    TipoAtividade.SISTEMA,
                    TipoAtividade.PERSONALIZADA_PUBLICA,
                    StatusModeracao.APROVADO,
                    termo, tags, pageable);
        } else {
            return atividadeRepository.findPublicasByNome(
                    TipoAtividade.SISTEMA,
                    TipoAtividade.PERSONALIZADA_PUBLICA,
                    StatusModeracao.APROVADO,
                    termo, pageable);
        }
    }

    //T7-Detalhe-Atividade-Pública – descrição, tags, avaliações, ações (Adicionar a Roteiro / Amei).
    @Transactional(readOnly = true)
    public Atividade obterDetalhesAtividadePublicaOuPropria(Long id, User usuarioLogado) {
        Atividade atividade = findByIdOrThrow(id);

        boolean isPublicaAprovada = atividade.getTipo() == TipoAtividade.SISTEMA ||
                (atividade.getTipo() == TipoAtividade.PERSONALIZADA_PUBLICA && atividade.getStatusModeracao() == StatusModeracao.APROVADO);

        boolean isPropria = usuarioLogado != null &&
                atividade.getCriador() != null &&
                Objects.equals(atividade.getCriador().getId(), usuarioLogado.getId());

        if (isPublicaAprovada || isPropria) {
            return atividade;
        } else {
            throw new SecurityException("Usuário não tem permissão para ver esta atividade."); // Ou use uma exceção customizada
        }
    }

    //T13-Criar-Atividade-Personalizada – título, descrição, local opcional, tags, visibilidade (público requer moderação).
    @Transactional
    public Atividade criarAtividadePersonalizada(Atividade novaAtividade, User usuarioLogado) {
        if (usuarioLogado == null) {
            throw new IllegalArgumentException("Usuário precisa estar logado para criar atividade.");
        }
        if (novaAtividade.getTipo() == TipoAtividade.SISTEMA) {
            throw new IllegalArgumentException("Usuário não pode criar atividade do tipo SISTEMA.");
        }
        if (!StringUtils.hasText(novaAtividade.getNome())) {
            throw new IllegalArgumentException("Nome da atividade não pode ser vazio.");
        }


        Atividade atividade = new Atividade();
        atividade.setNome(novaAtividade.getNome());
        atividade.setDescricao(novaAtividade.getDescricao());
        atividade.setLocal(novaAtividade.getLocal());
        atividade.setMidiaURL(novaAtividade.getMidiaURL());
        if (novaAtividade.getTags() != null) {
            atividade.setTags(new HashSet<>(novaAtividade.getTags()));
        }
        atividade.setCriador(usuarioLogado);
        atividade.setTipo(novaAtividade.getTipo());

        if (atividade.getTipo() == TipoAtividade.PERSONALIZADA_PUBLICA) {
            atividade.setStatusModeracao(StatusModeracao.PENDENTE);
        } else {
            atividade.setStatusModeracao(null);
        }

        return atividadeRepository.save(atividade);
    }

    @Transactional(readOnly = true)
    public Atividade findByIdOrThrow(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Atividade não encontrada com id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Atividade> listarAtividadesPendentesModeracao() {
        return atividadeRepository.findByTipoAndStatusModerao(TipoAtividade.PERSONALIZADA_PUBLICA, StatusModeracao.PENDENTE);
    }

    @Transactional
    public Atividade moderarAtividade(Long atividadeId, StatusModeracao novoStatus, User admin) {

        Atividade atividade = findByIdOrThrow(atividadeId);
        if (atividade.getTipo() != TipoAtividade.PERSONALIZADA_PUBLICA) {
            throw new IllegalArgumentException("Apenas atividades PERSONALIZADA_PUBLICA podem ser moderadas.");
        }
        if (novoStatus == StatusModeracao.PENDENTE) {
            throw new IllegalArgumentException("Não é possível definir o status como PENDENTE manualmente.");
        }
        atividade.setStatusModeracao(novoStatus);
        return atividadeRepository.save(atividade);
    }
}
