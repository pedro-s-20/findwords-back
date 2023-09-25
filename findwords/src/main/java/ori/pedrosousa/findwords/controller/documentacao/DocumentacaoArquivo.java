package ori.pedrosousa.findwords.controller.documentacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.config.exceptions.RegraDeNegocioException;
import ori.pedrosousa.findwords.dto.DocumentacaoDTO;
import ori.pedrosousa.findwords.dto.PageDTO;

import java.util.List;

public interface DocumentacaoArquivo {

    @Operation(summary = "Upload arquivo", description = "Adicionar repositório de arquivos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Arquivo adicionado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @PostMapping
    ResponseEntity<Void> uploadArchive(@RequestParam MultipartFile[] arquivo) throws RegraDeNegocioException;

    @Operation(summary = "Listar arquvios", description = "Lista todos os arquivos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Todos os Agendamentos foram listados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @GetMapping()
    ResponseEntity<PageDTO<DocumentacaoDTO>> list(@RequestParam Integer pagina, @RequestParam Integer tamanho);
}
