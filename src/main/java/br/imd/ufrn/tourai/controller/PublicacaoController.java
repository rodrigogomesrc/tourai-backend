package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.model.Publicacao;
import br.imd.ufrn.tourai.service.PublicacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/posts")
public class PublicacaoController {

    private final PublicacaoService publicacaoService;

    public PublicacaoController(PublicacaoService publicacaoService) {
        this.publicacaoService = publicacaoService;
    }

    @PostMapping
    public ResponseEntity<Publicacao> criarPublicacao(@RequestBody Publicacao publicacao) {
        Publicacao salva = publicacaoService.save(publicacao);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publicacao> buscarPorId(@PathVariable Integer id) {
        Publicacao publicacao = publicacaoService.findById(id);
        if (publicacao == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(publicacao);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Integer id) {
        Publicacao publicacao = publicacaoService.findById(id);
        if (publicacao == null) {
            return ResponseEntity.notFound().build();
        }
        publicacaoService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/recentes")
    public ResponseEntity<List<Publicacao>> listarMaisRecentes(
            @RequestParam(defaultValue = "10") int quantidade) {
        List<Publicacao> publicacoes = publicacaoService.getMaisRecentes(quantidade);
        return ResponseEntity.ok(publicacoes);
    }

    @GetMapping("/antigos")
    public ResponseEntity<List<Publicacao>> listarMaisAntigas(
            @RequestParam Instant antesDe,
            @RequestParam(defaultValue = "10") int quantidade) {
        List<Publicacao> publicacoes = publicacaoService.getMaisAntigas(antesDe, quantidade);
        return ResponseEntity.ok(publicacoes);
    }
}
