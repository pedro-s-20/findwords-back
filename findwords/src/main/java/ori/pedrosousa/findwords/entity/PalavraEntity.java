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
@Entity(name = "PALAVRA")
public class PalavraEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_palavra")
    private Long id;

    @Lob
    @Column(name="nome")
    private String nome;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinTable(name = "PALAVRA_X_DOCUMENTACAO",
            joinColumns = @JoinColumn(name = "id_palavra"),
            inverseJoinColumns = @JoinColumn(name = "id_documentacao"))
    private Set<DocumentacaoEntity> documentos;
}
