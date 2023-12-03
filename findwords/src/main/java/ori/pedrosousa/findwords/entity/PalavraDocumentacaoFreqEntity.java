package ori.pedrosousa.findwords.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "PALAVRA_DOCUMENTACAO_FREQ")
public class PalavraDocumentacaoFreqEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id_palavra_documentacao")
    private Long id;

    @Column(name = "id_palavra")
    private Long idPalavra;

    @Column(name = "id_documentacao")
    private Long idDocumentacao;

    @Column(name = "frequencia")
    private Long frequencia;
}