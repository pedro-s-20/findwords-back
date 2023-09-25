package ori.pedrosousa.findwords.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jsoup.Jsoup;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.config.exceptions.RegraDeNegocioException;
import ori.pedrosousa.findwords.dto.DocumentacaoDTO;
import ori.pedrosousa.findwords.dto.GraficoDTO;
import ori.pedrosousa.findwords.dto.PageDTO;
import ori.pedrosousa.findwords.entity.DocumentacaoEntity;
import ori.pedrosousa.findwords.repository.DocumentacaoRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DocumentacaoService {

    private final DocumentacaoRepository documentacaoRepository;
    private final ObjectMapper objectMapper;

    public void upload(MultipartFile[] arquivos) throws RegraDeNegocioException {
        try{
            List<MultipartFile> listaArquivos = List.of(arquivos);
            for (MultipartFile multipartFile:listaArquivos) {
                File file = multipartToFile(multipartFile, multipartFile.getOriginalFilename());
                String result = Jsoup.parse(FileUtils.readFileToString(file)).text().toLowerCase();

                String utf8EncodedString = StringEscapeUtils.unescapeHtml4(result);
                String textoNormalizado = normalizarTexto(utf8EncodedString);

                documentacaoRepository.save(DocumentacaoEntity.builder()
                        .nomeArquivo(file.getName())
                        .texto(textoNormalizado)
                        .build());
            }
        }catch(IOException e){
            throw new RegraDeNegocioException("Erro ao iterpretar o arquivo.");
        }
    }

    public PageDTO<DocumentacaoDTO> list(Integer pagina, Integer tamanho){
        Pageable solicitacaoPagina = PageRequest.of(pagina,tamanho);
        Page<DocumentacaoEntity> documentacao = documentacaoRepository.findAll(solicitacaoPagina);
        List<DocumentacaoEntity> documentacaoEntityList = documentacao.getContent().stream().toList();

        List<DocumentacaoDTO> documentacaoDTOList = documentacaoEntityList.stream()
                .map(item -> objectMapper.convertValue(item, DocumentacaoDTO.class))
                .collect(Collectors.toList());;

        return new PageDTO<>(documentacao.getTotalElements(),
                documentacao.getTotalPages(),
                pagina,
                tamanho,
                documentacaoDTOList);
    }

    public GraficoDTO gerarGraficoOcorrenciaPalavras(Integer tamanho){
        Map<String, Integer> todosElementos = retornarListaDeOcorrencias();

        Map<String, Integer> palavrasEFreq = todosElementos.entrySet()
                .stream()
                .limit(tamanho)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        DefaultCategoryDataset barra = new DefaultCategoryDataset();
        for (Map.Entry<String, Integer> item: palavrasEFreq.entrySet()) {
            barra.setValue(item.getValue(), item.getKey(), "");
        }

        JFreeChart grafico = ChartFactory.createBarChart3D("Ocorrências de palavras no repositório",
                "Palavras",
                "Ocorrências",
                barra,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);
        try {
            ChartUtilities.saveChartAsJPEG(
                    new File(System.getProperty("java.io.tmpdir")+"/grafico"), grafico, 1920, 1080);
            var file = new File(System.getProperty("java.io.tmpdir")+"/grafico");
            var path = Paths.get(file.getAbsolutePath());
            return GraficoDTO.builder().imagem(new ByteArrayResource(Files.readAllBytes(path))).tamanho(file.length()).build();
        } catch (IOException e) {
            new RegraDeNegocioException("Erro ao salvar imagem em arquivo.");
        }
        return null;
    }

    public List<String> listarOcorrenciaPalavras(Integer tamanho){

        Map<String, Integer> palavrasEFreq = retornarListaDeOcorrencias();

        List<String> palavrasEFrenquencias = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : palavrasEFreq.entrySet()) {
            palavrasEFrenquencias.add(entry.getKey() + ": " + entry.getValue());
        }

        return palavrasEFrenquencias.subList(0, tamanho-1);
    }

    private Map<String, Integer> retornarListaDeOcorrencias(){
        StringBuilder acervoEmTexto = new StringBuilder();
        List<DocumentacaoEntity> itens = documentacaoRepository.findAll();

        for (DocumentacaoEntity doc:itens) {
            acervoEmTexto.append(" ").append(doc.getTexto());
        }

        Map<String, Integer> frequenciaPalavras = contarPalavras(acervoEmTexto.toString());

        return ordenarPorFrequencia(frequenciaPalavras);
    }

    private Map<String, Integer> ordenarPorFrequencia(Map<String, Integer> mapa) {
        List<Map.Entry<String, Integer>> lista = new ArrayList<>(mapa.entrySet());

        Comparator<Map.Entry<String, Integer>> comparador = (entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue());

        lista.sort(comparador);

        Map<String, Integer> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : lista) {
            resultado.put(entry.getKey(), entry.getValue());
        }

        return resultado;
    }

    private Map<String, Integer> contarPalavras(String texto) {
        String[] palavras = texto.split("\\s+");

        Map<String, Integer> frequenciaPalavras = new HashMap<>();

        for (String palavra : palavras) {
            if(palavra.length() > 1){
                int frequencia = frequenciaPalavras.getOrDefault(palavra, 0);
                frequenciaPalavras.put(palavra, frequencia + 1);
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

}
