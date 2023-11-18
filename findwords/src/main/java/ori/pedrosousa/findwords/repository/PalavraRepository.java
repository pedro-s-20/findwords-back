package ori.pedrosousa.findwords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ori.pedrosousa.findwords.entity.PalavraEntity;

import java.util.Optional;

@Repository
public interface PalavraRepository extends JpaRepository<PalavraEntity, Long> {
    Optional<PalavraEntity> getPalavraEntityByNome(String nome);
}
