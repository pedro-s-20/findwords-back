package ori.pedrosousa.findwords.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.config.exceptions.RegraDeNegocioException;
import ori.pedrosousa.findwords.dto.DocumentacaoDTO;
import ori.pedrosousa.findwords.dto.OperadorLogicoEnum;
import ori.pedrosousa.findwords.dto.PageDTO;
import ori.pedrosousa.findwords.dto.TermoDTO;
import ori.pedrosousa.findwords.entity.DocumentacaoEntity;
import ori.pedrosousa.findwords.entity.PalavraEntity;
import ori.pedrosousa.findwords.repository.DocumentacaoRepository;
import ori.pedrosousa.findwords.repository.PalavraRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DocumentacaoService {

    private final DocumentacaoRepository documentacaoRepository;
    private final PalavraRepository palavraRepository;
    private final ObjectMapper objectMapper;

    public void upload(MultipartFile[] arquivos) throws RegraDeNegocioException {
        try{
            List<MultipartFile> listaArquivos = List.of(arquivos);
            for (MultipartFile multipartFile:listaArquivos) {
                File file = multipartToFile(multipartFile, multipartFile.getOriginalFilename());
                String result = Jsoup.parse(FileUtils.readFileToString(file)).text().toLowerCase();

                String unescapeHtml4 = StringEscapeUtils.unescapeHtml4(result);
                String textoNormalizado = normalizarTexto(unescapeHtml4);

                DocumentacaoEntity documentacaoAdicionada = documentacaoRepository.save(DocumentacaoEntity.builder()
                        .nomeArquivo(file.getName())
                        .palavras(new HashSet<>())
                        .arquivo(Files.readAllBytes(file.toPath()))
                        .build());

                separarPalavrasDocumento(documentacaoAdicionada, textoNormalizado);
            }
        }catch(IOException e){
            throw new RegraDeNegocioException("Erro ao iterpretar o arquivo: " + e);
        }
    }

    public PageDTO<DocumentacaoDTO> list(Integer pagina, Integer tamanho) throws RegraDeNegocioException {
        Pageable solicitacaoPagina = PageRequest.of(pagina,tamanho);
        Page<DocumentacaoEntity> documentacao = documentacaoRepository.findAll(solicitacaoPagina);
        List<DocumentacaoEntity> documentacaoEntityList = documentacao.getContent().stream().toList();

        if(documentacaoEntityList.isEmpty()){
            throw new RegraDeNegocioException("Nenhum elemento cadastrado. Faça upload de arquivo.");
        }

        List<DocumentacaoDTO> documentacaoDTOList = documentacaoEntityList.stream()
                .map(item -> objectMapper.convertValue(item, DocumentacaoDTO.class))
                .collect(Collectors.toList());;

        return new PageDTO<>(documentacao.getTotalElements(),
                documentacao.getTotalPages(),
                pagina,
                tamanho,
                documentacaoDTOList);
    }

    public byte[] download(Long id) throws RegraDeNegocioException{
        Optional<DocumentacaoEntity> doc = documentacaoRepository.findById(id);
        if (doc.isEmpty()) {
            throw new RegraDeNegocioException("Documento não encontrado");
        }
        return doc.get().getArquivo();
    }

    public PageDTO<DocumentacaoDTO> listByBooleanSearch(Integer pagina, Integer tamanho, List<TermoDTO> termos) throws RegraDeNegocioException {

        if(termos.get(0).getPalavra().isBlank() && termos.get(0).getOperadorAnterior() == OperadorLogicoEnum.NOT){
            termos.stream().skip(1).forEach(x -> x.setPalavra(normalizarTexto(x.getPalavra().toLowerCase())));
        }else{
            termos.forEach(x -> x.setPalavra(normalizarTexto(x.getPalavra().toLowerCase())));
        }

        Set<DocumentacaoEntity> documentacaoEntityList = new HashSet<>(documentacaoRepository.findAll());

        if(documentacaoEntityList.isEmpty()){
            throw new RegraDeNegocioException("Nenhum elemento cadastrado. Faça upload de arquivo.");
        }

        if(termos.isEmpty()){
            throw new RegraDeNegocioException("Nenhum termo para pesquisa encontrado.");
        }

        Set<PalavraEntity> palavrasTotais = new HashSet<>(palavraRepository.findAll());
        List<PalavraEntity> palavrasPertecentesTemp = new ArrayList<>();

        Set<DocumentacaoEntity> documentacaoTotal = new HashSet<>(documentacaoEntityList);
        Set<DocumentacaoEntity> documentacaoARemover = new HashSet<>();

        for (int i = 0; i < termos.size(); i++) {
            TermoDTO termoAtual = termos.get(i);
            if(termos.size() == 1) {
                for (PalavraEntity palavra : palavrasTotais) {
                    if (!termoAtual.getPalavra().isBlank()
                            && palavra.getNome().equals(termoAtual.getPalavra())) {
                        palavrasPertecentesTemp.add(palavra);
                        break;
                    }
                }
                if(palavrasPertecentesTemp.isEmpty()){
                    palavrasPertecentesTemp.add(PalavraEntity.builder().nome(termoAtual.getPalavra()).build());
                }
                if (termoAtual.getOperadorAnterior() == OperadorLogicoEnum.NOT) {
                    for (DocumentacaoEntity doc : documentacaoEntityList) {
                        if (doc.getPalavras().contains(palavrasPertecentesTemp.get(0))) {
                            documentacaoARemover.add(doc);
                        }
                    }
                    documentacaoEntityList.removeAll(documentacaoARemover);
                } else {
                    for (DocumentacaoEntity doc : documentacaoEntityList) {
                        for (PalavraEntity palavra : palavrasPertecentesTemp) {
                            if (!doc.getPalavras().contains(palavra)) {
                                documentacaoARemover.add(doc);
                            }
                        }
                    }
                    documentacaoEntityList.removeAll(documentacaoARemover);
                }
                continue;
            }

            if(i < (termos.size()-1)){
                TermoDTO termoProx = termos.get(i+1);
                OperadorLogicoEnum operador = termos.get(i).getOperadorAnterior();

                if(termos.get(0).getPalavra().isBlank()){
                    for (PalavraEntity palavra : palavrasTotais) {
                        if(palavra.getNome().equals(termoProx.getPalavra())){
                            palavrasPertecentesTemp.add(palavra);
                        }
                    }
                }else{
                    for (PalavraEntity palavra : palavrasTotais) {
                        if(palavra.getNome().equals(termoAtual.getPalavra())
                                || palavra.getNome().equals(termoProx.getPalavra())){
                            palavrasPertecentesTemp.add(palavra);
                        }
                    }
                }

                if(palavrasPertecentesTemp.isEmpty()){
                    if (termos.get(0).getPalavra().isBlank()) {
                        palavrasPertecentesTemp.add(PalavraEntity.builder().nome(termoProx.getPalavra()).build());
                    }else{
                        palavrasPertecentesTemp.add(PalavraEntity.builder().nome(termoAtual.getPalavra()).build());
                        palavrasPertecentesTemp.add(PalavraEntity.builder().nome(termoProx.getPalavra()).build());
                    }
                }
                if(palavrasPertecentesTemp.size() == 1 && !termos.get(0).getPalavra().isBlank()){
                    if(palavrasPertecentesTemp.get(0).getNome().equals(termoAtual.getPalavra())){
                        palavrasPertecentesTemp.add(PalavraEntity.builder().nome(termoProx.getPalavra()).build());
                    }else{
                        palavrasPertecentesTemp.add(PalavraEntity.builder().nome(termoAtual.getPalavra()).build());
                    }
                }
                switch (operador) {
                    case AND -> {
                        if (palavrasPertecentesTemp.size() == 1) {
                            for (DocumentacaoEntity doc : documentacaoEntityList) {
                                if (!doc.getPalavras().contains(palavrasPertecentesTemp.get(0))) {
                                    documentacaoARemover.add(doc);
                                }
                            }
                            documentacaoEntityList.removeAll(documentacaoARemover);
                        } else {
                            for (DocumentacaoEntity doc : documentacaoEntityList) {
                                if (!doc.getPalavras().contains(palavrasPertecentesTemp.get(0))
                                        || !doc.getPalavras().contains(palavrasPertecentesTemp.get(1))) {
                                    documentacaoARemover.add(doc);
                                }
                            }
                            documentacaoEntityList.removeAll(documentacaoARemover);
                        }
                    }
                    case OR -> {
                        if (palavrasPertecentesTemp.size() == 1) {
                            for (DocumentacaoEntity doc : documentacaoTotal) {
                                if (doc.getPalavras().contains(palavrasPertecentesTemp.get(0))) {
                                    documentacaoEntityList.add(doc);
                                }
                            }
                        } else {
                            for (DocumentacaoEntity doc : documentacaoTotal) {
                                if (doc.getPalavras().contains(palavrasPertecentesTemp.get(0))
                                        && doc.getPalavras().contains(palavrasPertecentesTemp.get(1))) {
                                    documentacaoEntityList.add(doc);
                                }
                            }
                        }
                    }
                    case NOT -> {
                        if (palavrasPertecentesTemp.size() == 1) {
                            for (DocumentacaoEntity doc : documentacaoEntityList) {
                                if (doc.getPalavras().contains(palavrasPertecentesTemp.get(0))) {
                                    documentacaoARemover.add(doc);
                                }
                            }
                            documentacaoEntityList.removeAll(documentacaoARemover);
                        } else {
                            for (DocumentacaoEntity doc : documentacaoEntityList) {
                                for (int j = 0; j < 2; j++) {
                                    if (!doc.getPalavras().contains(palavrasPertecentesTemp.get(0))
                                            && doc.getPalavras().contains(palavrasPertecentesTemp.get(1))) {
                                        documentacaoARemover.add(doc);
                                    }
                                }
                            }
                            documentacaoEntityList.removeAll(documentacaoARemover);
                        }
                    }
                }
            }
        }

        List<DocumentacaoDTO> documentacaoDTOList = documentacaoEntityList.stream()
                .map(item -> objectMapper.convertValue(item, DocumentacaoDTO.class))
                .collect(Collectors.toList());

        documentacaoARemover.clear();
        palavrasPertecentesTemp.clear();

        return criarPageDTO(documentacaoDTOList, tamanho, pagina);
    }

    private Map<String, Integer> separarPalavrasDocumento(DocumentacaoEntity documentacaoEntity, String textoNormalizado) {
        String[] palavras = textoNormalizado.split("\\s+");

        Map<String, Integer> frequenciaPalavras = new HashMap<>();

        List<String> palavrasBanco = palavraRepository.findAll().stream().map(PalavraEntity::getNome).collect(Collectors.toList());

        for (String palavra : palavras) {
            if(palavra.length() > 1 && !palavrasBanco.contains(palavra)){
                PalavraEntity palavraEntityNova = new PalavraEntity();
                palavraEntityNova.setNome(palavra);

                Set<DocumentacaoEntity> documentacao = new HashSet<>();
                documentacao.add(documentacaoEntity);
                palavraEntityNova.setDocumentos(documentacao);
                palavraRepository.save(palavraEntityNova);

                palavrasBanco.add(palavra);

            }else if(palavra.length() > 1 && palavrasBanco.contains(palavra)){
                Optional<PalavraEntity> palavraArmazenada = palavraRepository.getPalavraEntityByNome(palavra);
                if (palavraArmazenada.isPresent() &&
                        !palavraArmazenada.get().getDocumentos().contains(documentacaoEntity)){
                    palavraArmazenada.get().getDocumentos().add(documentacaoEntity);
                }
            }
        }
        return frequenciaPalavras;
    }

    private File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir")+"/" + fileName);
        multipart.transferTo(convFile);
        return convFile;
    }

    private String normalizarTexto(String text) {
        String textoSemAcentos = Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");

        textoSemAcentos = textoSemAcentos.replaceAll("ç", "c");
        textoSemAcentos = textoSemAcentos.replaceAll("\\p{Punct}", "");
        textoSemAcentos = textoSemAcentos.replaceAll("\\p{Digit}", "");

        return textoSemAcentos;
    }

    private <T> PageDTO<T> criarPageDTO(List<T> elementos, int tamanhoDaPagina, int numeroDaPagina) {
        PageDTO<T> pageDTO = new PageDTO<>();

        pageDTO.setTotalElementos((long) elementos.size());
        int quantidadePaginas = (int) Math.ceil((double) elementos.size() / tamanhoDaPagina);
        pageDTO.setQuantidadePaginas(quantidadePaginas);

        pageDTO.setPagina(numeroDaPagina);

        pageDTO.setTamanho(tamanhoDaPagina);

        int indiceInicial = numeroDaPagina * tamanhoDaPagina;
        int indiceFinal = Math.min(indiceInicial + tamanhoDaPagina, elementos.size());

        if(indiceInicial > indiceFinal){
            pageDTO.setElementos((List<T>) new ArrayList<DocumentacaoDTO>());
        }else {
            pageDTO.setElementos(elementos.subList(indiceInicial, indiceFinal));
        }

        return pageDTO;
    }

}
