package ori.pedrosousa.findwords.controller.documentacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.config.exceptions.RegraDeNegocioException;
import ori.pedrosousa.findwords.dto.DocumentacaoDTO;
import ori.pedrosousa.findwords.dto.PageDTO;
import ori.pedrosousa.findwords.dto.TermoDTO;

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
    ResponseEntity<List<String>> uploadArchive(@RequestParam("files") MultipartFile[] arquivos) throws RegraDeNegocioException;

    @Operation(summary = "Listar arquvios", description = "Lista todos os arquivos")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Todos os arquivos foram listados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @GetMapping()
    ResponseEntity<PageDTO<DocumentacaoDTO>> list(@RequestParam Integer pagina,
                                                  @RequestParam Integer tamanho)  throws RegraDeNegocioException;

    @Operation(summary = "Download arquivo", description = "Faz o download de determinado arquivo, pelo ID")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Download realizado com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @GetMapping("/download-archive/{id}")
    ResponseEntity<byte[]> downloadArchive(@PathVariable("id") Long id) throws RegraDeNegocioException;


    @Operation(summary = "Listar busca booleana", description = "Listar arquivos por método de busca booleana")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Arquivos encontrados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @PostMapping("/boolean-search")
    ResponseEntity<PageDTO<DocumentacaoDTO>> listByBooleanSearch(@RequestParam Integer pagina,
                                                                 @RequestParam Integer tamanho,
                                                                 @RequestBody List<TermoDTO> termos)  throws RegraDeNegocioException;

    @Operation(summary = "Listar busca vetorial ranqueada", description = "Listar arquivos por método de busca vetoral com ranking")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "Arquivos encontrados com sucesso"),
                    @ApiResponse(responseCode = "403", description = "Você não tem permissão para acessar este recurso"),
                    @ApiResponse(responseCode = "500", description = "Foi gerada uma exceção")
            }
    )
    @PostMapping("/vetorial-ranking-search")
    ResponseEntity<PageDTO<DocumentacaoDTO>> listByVetorialRankingSearch(@RequestParam Integer pagina,
                                                                         @RequestParam Integer tamanho,
                                                                         @RequestBody String pesquisa)  throws RegraDeNegocioException;
}
