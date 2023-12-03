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
import ori.pedrosousa.findwords.entity.PalavraDocumentacaoFreqEntity;
import ori.pedrosousa.findwords.entity.PalavraEntity;
import ori.pedrosousa.findwords.repository.DocumentacaoRepository;
import ori.pedrosousa.findwords.repository.PalavraDocumentacaoFreqRepository;
import ori.pedrosousa.findwords.repository.PalavraRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DocumentacaoService {

    private final DocumentacaoRepository documentacaoRepository;
    private final PalavraRepository palavraRepository;
    private final PalavraDocumentacaoFreqRepository palavraDocumentacaoFreqRepository;
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
                .sorted(Comparator.comparingInt(o -> o.getId().intValue()))
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

        if(termos.get(0).getPalavra().isBlank() && termos.get(0).getOperadorProximo() == OperadorLogicoEnum.NOT){
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
        Set<DocumentacaoEntity> documentacaoTotal = new HashSet<>(documentacaoEntityList);
        Set<DocumentacaoEntity> documentacaoARemover = new HashSet<>();
        PalavraEntity palavraPesquisadaTemp = null;

        for (int i = 0; i < termos.size()-1; i++) {
            TermoDTO termoProx = termos.get(i+1);
            OperadorLogicoEnum operador = termos.get(i).getOperadorProximo();

            for (PalavraEntity palavra : palavrasTotais) {
                if(palavra.getNome().equals(termoProx.getPalavra())){
                    palavraPesquisadaTemp = palavra;
                }
            }

            if(palavraPesquisadaTemp == null){
                palavraPesquisadaTemp = PalavraEntity.builder().nome(termoProx.getPalavra()).build();
            }

            switch (operador) {
                case AND -> {
                    for (DocumentacaoEntity doc : documentacaoEntityList) {
                        if (!doc.getPalavras().contains(palavraPesquisadaTemp)) {
                            documentacaoARemover.add(doc);
                        }
                    }
                    documentacaoEntityList.removeAll(documentacaoARemover);
                }
                case OR -> {
                    for (DocumentacaoEntity doc : documentacaoTotal) {
                        if (doc.getPalavras().contains(palavraPesquisadaTemp)) {
                            documentacaoEntityList.add(doc);
                        }
                    }
                }
                case NOT -> {
                    for (DocumentacaoEntity doc : documentacaoEntityList) {
                        if (doc.getPalavras().contains(palavraPesquisadaTemp)) {
                            documentacaoARemover.add(doc);
                        }
                    }
                    documentacaoEntityList.removeAll(documentacaoARemover);
                }
            }
            documentacaoARemover.clear();
            palavraPesquisadaTemp = null;
        }

        List<DocumentacaoDTO> documentacaoDTOList = documentacaoEntityList.stream()
                .map(item -> objectMapper.convertValue(item, DocumentacaoDTO.class))
                .sorted(Comparator.comparingInt(o -> o.getId().intValue()))
                .collect(Collectors.toList());


        return criarPageDTO(documentacaoDTOList, tamanho, pagina);
    }

    public PageDTO<DocumentacaoDTO> listByVetorialRankingSearch(Integer pagina, Integer tamanho, String pesquisa) {


        return null;
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

                PalavraDocumentacaoFreqEntity palavraDocumentacaoFreq = new PalavraDocumentacaoFreqEntity();
                palavraDocumentacaoFreq.setIdPalavra(palavraEntityNova.getId());
                palavraDocumentacaoFreq.setIdDocumentacao(documentacaoEntity.getId());
                palavraDocumentacaoFreq.setFrequencia(0L);
                palavraDocumentacaoFreqRepository.save(palavraDocumentacaoFreq);

            }else if(palavra.length() > 1 && palavrasBanco.contains(palavra)){
                Optional<PalavraEntity> palavraArmazenada = palavraRepository.getPalavraEntityByNome(palavra);
                if (palavraArmazenada.isPresent() &&
                        !palavraArmazenada.get().getDocumentos().contains(documentacaoEntity)){
                    palavraArmazenada.get().getDocumentos().add(documentacaoEntity);

                    PalavraDocumentacaoFreqEntity palavraDocumentacaoFreq = new PalavraDocumentacaoFreqEntity();
                    palavraDocumentacaoFreq.setIdPalavra(palavraArmazenada.get().getId());
                    palavraDocumentacaoFreq.setIdDocumentacao(documentacaoEntity.getId());
                    palavraDocumentacaoFreq.setFrequencia(0L);
                    palavraDocumentacaoFreqRepository.save(palavraDocumentacaoFreq);
                }else{
                    Optional<PalavraDocumentacaoFreqEntity> palavraDocumentacaoFreqEntityOptional = palavraDocumentacaoFreqRepository
                            .getPalavraDocumentacaoFreqEntityByIdPalavraAndIdDocumentacao(palavraArmazenada.get().getId(), documentacaoEntity.getId());
                    if(palavraDocumentacaoFreqEntityOptional.isPresent()){
                        PalavraDocumentacaoFreqEntity palavraDocumentacaoFreq = palavraDocumentacaoFreqEntityOptional.get();
                        palavraDocumentacaoFreq.setFrequencia(palavraDocumentacaoFreq.getFrequencia()+1L);
                        palavraDocumentacaoFreqRepository.save(palavraDocumentacaoFreq);
                    }
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
