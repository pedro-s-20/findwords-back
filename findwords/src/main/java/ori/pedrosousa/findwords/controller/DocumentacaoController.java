package ori.pedrosousa.findwords.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.config.exceptions.RegraDeNegocioException;
import ori.pedrosousa.findwords.controller.documentacao.DocumentacaoArquivo;
import ori.pedrosousa.findwords.service.DocumentacaoService;

@RestController
@RequestMapping("/arquivo")
@RequiredArgsConstructor
public class DocumentacaoController implements DocumentacaoArquivo {

    private final DocumentacaoService documentacaoService;

    @Override
    public ResponseEntity<Void> uploadArchive(MultipartFile arquivo) throws RegraDeNegocioException {
        documentacaoService.upload(arquivo);
        return ResponseEntity.ok().build();
    }
}
