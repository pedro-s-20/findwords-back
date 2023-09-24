package ori.pedrosousa.findwords.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ori.pedrosousa.findwords.repository.DocumentacaoRepository;

@Service
@RequiredArgsConstructor
public class DocumentacaoService {

    private final DocumentacaoRepository documentacaoRepository;

    public void upload(MultipartFile arquivo) {

    }
}
