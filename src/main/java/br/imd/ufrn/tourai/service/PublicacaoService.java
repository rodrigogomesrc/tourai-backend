package br.imd.ufrn.tourai.service;

import br.imd.ufrn.tourai.model.Publicacao;
import br.imd.ufrn.tourai.repository.PublicacaoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PublicacaoService {

    private PublicacaoRepository publicacaoRepository;

    public PublicacaoService(PublicacaoRepository publicacaoRepository) {
        this.publicacaoRepository = publicacaoRepository;
    }

    public Publicacao save(Publicacao publicacao) {
        Instant now = Instant.now();
        publicacao.setDataPublicacao(now);
        return publicacaoRepository.save(publicacao);
    }

    public Publicacao findById(Integer id) {
        return publicacaoRepository.findById(id).orElse(null);
    }

    public void deleteById(Integer id) {
        publicacaoRepository.deleteById(id);
    }

    public List<Publicacao> getMaisRecentes(int quantidade) {
        return publicacaoRepository.findUltimas(PageRequest.of(0, quantidade));
    }

    public List<Publicacao> getMaisAntigas(Instant dataUltimo, int quantidade) {
        return publicacaoRepository.findMaisAntigas(dataUltimo, PageRequest.of(0, quantidade));
    }


}
