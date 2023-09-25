package ori.pedrosousa.findwords.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.io.ByteArrayResource;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GraficoDTO {
    Long tamanho;
    ByteArrayResource imagem;
}
