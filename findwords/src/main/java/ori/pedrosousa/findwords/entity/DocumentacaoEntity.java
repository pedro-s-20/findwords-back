package ori.pedrosousa.findwords.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "DOCUMENTACAO")
public class DocumentacaoEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_documentacao")
    private Long id;

    @Column(name="nome")
    private String nomeArquivo;

    @JsonIgnore
    @Lob
    @Column(name="arquivo")
    private byte[] arquivo;

    @JsonIgnore
    @ManyToMany(mappedBy = "documentos", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    private Set<PalavraEntity> palavras;
}
