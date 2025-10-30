package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.model.*;
import br.imd.ufrn.tourai.repository.RoteiroRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoteiroService {

    @Autowired
    private RoteiroRepository roteiroRepository;

    @Autowired
    private AtividadeService atividadeService;

    //T8-Meus-Roteiros – listagem (rascunho, privado, compartilhado, público).
    @Transactional(readOnly = true)
    public List<Roteiro> listarMeusRoteiros(User usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido ou não logado.");
        }
        return roteiroRepository.findByUsuarioIdOrderByTituloAsc(usuarioLogado.getId());
    }

    //T9-Editor-Roteiro – formulário + adicionar atividades (catálogo/personalizadas), ordenar lista, definir visibilidade.
    @Transactional
    public Roteiro criarRoteiro(Roteiro novoRoteiro, User usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getId() == null) {
            throw new IllegalArgumentException("Usuário precisa estar logado para criar roteiro.");
        }
        if (!StringUtils.hasText(novoRoteiro.getTitulo())) {
            throw new IllegalArgumentException("Título do roteiro não pode ser vazio.");
        }

        Roteiro roteiro = new Roteiro();
        roteiro.setTitulo(novoRoteiro.getTitulo());
        roteiro.setDescricao(novoRoteiro.getDescricao());
        if (novoRoteiro.getTags() != null) {
            roteiro.setTags(new HashSet<>(novoRoteiro.getTags()));
        }
        roteiro.setUser(usuarioLogado);
        roteiro.setVisibilidade(novoRoteiro.getVisibilidade() != null ? novoRoteiro.getVisibilidade() : VisibilidadeRoteiro.PRIVADO);

        if (roteiro.getVisibilidade() == VisibilidadeRoteiro.PUBLICO) {
            roteiro.setStatus(StatusModeracao.PENDENTE); // [cite: 33]
        } else {
            roteiro.setStatus(null); // Ou APROVADO se não públicos não precisam
        }

        if (novoRoteiro.getAtividades() != null && !novoRoteiro.getAtividades().isEmpty()) {
            Set<Long> atividadesIds = novoRoteiro.getAtividades().stream()
                    .map(Atividade::getId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            Set<Atividade> atividadesToAdd = atividadesIds.stream()
                    .map(id -> atividadeService.findByIdOrThrow(id))
                    .collect(Collectors.toSet());
            atividadesToAdd.forEach(roteiro::addAtividade); // Usa o helper
        }


        return roteiroRepository.save(roteiro);
    }

    @Transactional
    public Roteiro atualizarRoteiro(Long roteiroId, Roteiro dadosAtualizacao, User usuarioLogado) {
        Roteiro roteiroExistente = findByIdAndOwnerOrThrow(roteiroId, usuarioLogado);

        if (StringUtils.hasText(dadosAtualizacao.getTitulo())) {
            roteiroExistente.setTitulo(dadosAtualizacao.getTitulo());
        }
        roteiroExistente.setDescricao(dadosAtualizacao.getDescricao());

        if (dadosAtualizacao.getTags() != null) {
            roteiroExistente.setTags(new HashSet<>(dadosAtualizacao.getTags()));
        }

        VisibilidadeRoteiro visibilidadeAntiga = roteiroExistente.getVisibilidade();
        VisibilidadeRoteiro visibilidadeNova = dadosAtualizacao.getVisibilidade();

        if (visibilidadeNova != null && visibilidadeAntiga != visibilidadeNova) {
            roteiroExistente.setVisibilidade(visibilidadeNova);
            if (visibilidadeNova == VisibilidadeRoteiro.PUBLICO) {
                if (roteiroExistente.getStatus() != StatusModeracao.APROVADO) {
                    roteiroExistente.setStatus(StatusModeracao.PENDENTE);
                }
            } else {
                roteiroExistente.setStatus(null);
            }
        }
        return roteiroRepository.save(roteiroExistente);
    }

    @Transactional
    public void deletarRoteiro(Long roteiroId, User usuarioLogado) {
        Roteiro roteiro = findByIdAndOwnerOrThrow(roteiroId, usuarioLogado);

        new HashSet<>(roteiro.getAtividades()).forEach(roteiro::removeAtividade);

        roteiroRepository.delete(roteiro);
    }

    //T6-Detalhe-Roteiro-Público – capa, descrição, atividades, avaliações, botão Salvar/Amei e Converter em Itinerário.
    //T9-Editor-Roteiro – formulário + adicionar atividades (catálogo/personalizadas), ordenar lista, definir visibilidade.
    @Transactional(readOnly = true)
    public Roteiro obterDetalhesRoteiro(Long roteiroId, User usuarioLogado) {

        Roteiro roteiro = roteiroRepository.findByIdWithAtividades(roteiroId);
        if (roteiro == null) {
            throw new EntityNotFoundException("Roteiro não encontrado com id: " + roteiroId);
        }

        // Regra de acesso: Ou é público e aprovado, ou pertence ao usuário logado
        boolean isPublicoAprovado = roteiro.getVisibilidade() == VisibilidadeRoteiro.PUBLICO
                && roteiro.getStatus() == StatusModeracao.APROVADO;
        boolean pertenceAoUsuario = usuarioLogado != null &&
                roteiro.getUser() != null && // Checagem de segurança
                Objects.equals(roteiro.getUser().getId(), usuarioLogado.getId());

        if (!isPublicoAprovado && !pertenceAoUsuario) {
            throw new SecurityException("Acesso negado ao roteiro " + roteiroId);
        }

        // Atividades já foram carregadas pelo findByIdWithAtividades
        return roteiro;
    }

    @Transactional
    public Roteiro adicionarAtividadeAoRoteiro(Long roteiroId, Long atividadeId, User usuarioLogado) {
        Roteiro roteiro = findByIdAndOwnerOrThrow(roteiroId, usuarioLogado);
        Atividade atividade = atividadeService.findByIdOrThrow(atividadeId);

        // Validação: Atividade deve ser PÚBLICA/SISTEMA ou pertencer ao mesmo usuário
        boolean isPublicaOuSistema = atividade.getTipo() == TipoAtividade.SISTEMA ||
                (atividade.getTipo() == TipoAtividade.PERSONALIZADA_PUBLICA && atividade.getStatusModeracao() == StatusModeracao.APROVADO);
        boolean isPropriaDoUsuario = atividade.getCriador() != null && Objects.equals(atividade.getCriador().getId(), usuarioLogado.getId());

        if (!isPublicaOuSistema && !isPropriaDoUsuario) {
            throw new SecurityException("Não é possível adicionar esta atividade ao roteiro.");
        }


        roteiro.addAtividade(atividade);
        return roteiroRepository.save(roteiro);
    }

    @Transactional
    public Roteiro removerAtividadeDoRoteiro(Long roteiroId, Long atividadeId, User usuarioLogado) {
        Roteiro roteiro = findByIdAndOwnerOrThrow(roteiroId, usuarioLogado);
        Atividade atividade = atividadeService.findByIdOrThrow(atividadeId);

        if (!roteiro.getAtividades().contains(atividade)) {
            throw new IllegalArgumentException("Atividade " + atividadeId + " não pertence ao roteiro " + roteiroId);
        }

        roteiro.removeAtividade(atividade);
        return roteiroRepository.save(roteiro);
    }

    private Roteiro findByIdAndOwnerOrThrow(Long roteiroId, User usuarioLogado) {
        if (usuarioLogado == null || usuarioLogado.getId() == null) {
            throw new IllegalArgumentException("Usuário inválido ou não logado.");
        }
        return roteiroRepository.findById(roteiroId)
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(usuarioLogado.getId()))
                .orElseThrow(() -> new EntityNotFoundException("Roteiro não encontrado ou não pertence ao usuário: " + roteiroId));
    }

    @Transactional(readOnly = true)
    public List<Roteiro> listarRoteirosPendentesModeracao() {
        return roteiroRepository.findByVisibilidadeAndStatusModeracao(VisibilidadeRoteiro.PUBLICO, StatusModeracao.PENDENTE);
    }

    @Transactional
    public Roteiro moderarRoteiro(Long roteiroId, StatusModeracao novoStatus, User admin) {

        Roteiro roteiro = roteiroRepository.findById(roteiroId)
                .orElseThrow(() -> new EntityNotFoundException("Roteiro não encontrado: " + roteiroId));

        if (roteiro.getVisibilidade() != VisibilidadeRoteiro.PUBLICO) {
            throw new IllegalArgumentException("Apenas roteiros PÚBLICOS podem ser moderados.");
        }
        if (novoStatus == StatusModeracao.PENDENTE) {
            throw new IllegalArgumentException("Não é possível definir o status como PENDENTE manualmente.");
        }
        roteiro.setStatus(novoStatus);
        return roteiroRepository.save(roteiro);
    }
}



