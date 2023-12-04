package ori.pedrosousa.findwords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ori.pedrosousa.findwords.entity.PalavraDocumentacaoFreqEntity;

import java.util.Optional;

@Repository
public interface PalavraDocumentacaoFreqRepository extends JpaRepository<PalavraDocumentacaoFreqEntity, Long> {
    Optional<PalavraDocumentacaoFreqEntity> getPalavraDocumentacaoFreqEntityByIdPalavraAndIdDocumentacao(Long idPalavra, Long idDocumentacao);

    @Query("SELECT MAX(p.frequencia) FROM PALAVRA_DOCUMENTACAO_FREQ p WHERE p.idPalavra = :idPalavra")
    long maxValueByIdPalavra(Long idPalavra);

    @Query("SELECT COUNT(p) FROM PALAVRA_DOCUMENTACAO_FREQ p WHERE p.idPalavra = :idPalavra")
    long countByIdPalavra(Long idPalavra);
}
