package br.imd.ufrn.tourai.controller;

import br.imd.ufrn.tourai.service.ArquivoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/arquivos")
public class ArquivoController {

    private static final long MAX_UPLOAD_SIZE = 20 * 1024 * 1024;

    private final ArquivoService arquivoService;

    public ArquivoController(ArquivoService arquivoService) {
        this.arquivoService = arquivoService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadArquivo(@RequestParam("arquivo") MultipartFile arquivo) {

        try {
            if (arquivo == null || arquivo.isEmpty()) {
                return ResponseEntity.badRequest().body("Nenhum arquivo enviado.");
            }


            if (arquivo.getSize() > MAX_UPLOAD_SIZE) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body("Arquivo maior que o limite permitido de 20MB.");
            }

            String url = arquivoService.salvarArquivo(arquivo);
            return ResponseEntity.ok(url);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erro ao processar upload: " + e.getMessage());
        }
    }

}
