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

    @Operation(summary = "Upload arquivos", description = "Adicionar repositório de arquivos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Arquivos adicionados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @PostMapping(consumes = { "multipart/form-data" })
    ResponseEntity<Void> uploadArchive(@RequestParam MultipartFile[] arquivos) throws RegraDeNegocioException;

    @Operation(summary = "Listar arquvios", description = "Lista todos os arquivos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Todos os arquivos foram listados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @GetMapping()
    ResponseEntity<PageDTO<DocumentacaoDTO>> list(@RequestParam Integer pagina, @RequestParam Integer tamanho)  throws RegraDeNegocioException;

    @Operation(summary = "Listar ocorrência de palavras",
            description = "Lista ocorrência de palavras, somente os primeiros x (tamnho) elementos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Palavras e frequência listados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @GetMapping("/ocorrencia-palavras")
    ResponseEntity<List<String>> listarOcorrenciaPalavras(@RequestParam Integer tamanho) throws RegraDeNegocioException;

    @Operation(summary = "Gerar gráfico com ocorrência de palavras",
            description = "Gera gráfico com ocorrência de palavras, somente os primeiros x (tamnho) elementos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Gráfico gerado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @GetMapping("/ocorrencia-palavras/grafico")
    ResponseEntity<?> gerarGraficoOcorrenciaPalavras(@RequestParam Integer tamanho) throws RegraDeNegocioException;
}
