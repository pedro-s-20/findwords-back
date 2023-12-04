package ori.pedrosousa.findwords.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ori.pedrosousa.findwords.entity.DocumentacaoEntity;

@Repository
public interface DocumentacaoRepository extends JpaRepository<DocumentacaoEntity, Long> {
    long countBy();
}
