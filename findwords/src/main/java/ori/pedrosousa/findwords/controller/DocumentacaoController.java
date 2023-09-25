package ori.pedrosousa.findwords.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.config.exceptions.RegraDeNegocioException;
import ori.pedrosousa.findwords.controller.documentacao.DocumentacaoArquivo;
import ori.pedrosousa.findwords.dto.DocumentacaoDTO;
import ori.pedrosousa.findwords.dto.PageDTO;
import ori.pedrosousa.findwords.service.DocumentacaoService;

import java.util.List;

@RestController
@RequestMapping("/arquivo")
@RequiredArgsConstructor
public class DocumentacaoController implements DocumentacaoArquivo {

    private final DocumentacaoService documentacaoService;

    @Override
    public ResponseEntity<Void> uploadArchive(MultipartFile[] arquivos) throws RegraDeNegocioException {
        documentacaoService.upload(arquivos);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<PageDTO<DocumentacaoDTO>> list(Integer pagina, Integer tamanho) {
        return new ResponseEntity<>(documentacaoService.list(pagina, tamanho), HttpStatus.OK);
    }
}
