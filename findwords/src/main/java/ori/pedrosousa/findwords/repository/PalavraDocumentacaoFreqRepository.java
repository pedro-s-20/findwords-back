package ori.pedrosousa.findwords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ori.pedrosousa.findwords.entity.PalavraDocumentacaoFreqEntity;

import java.util.Optional;

@Repository
public interface PalavraDocumentacaoFreqRepository extends JpaRepository<PalavraDocumentacaoFreqEntity, Long> {
    Optional<PalavraDocumentacaoFreqEntity> getPalavraDocumentacaoFreqEntityByIdPalavraAndIdDocumentacao(Long idPalavra, Long idDocumentacao);
}
