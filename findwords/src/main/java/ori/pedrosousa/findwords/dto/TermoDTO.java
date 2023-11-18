package ori.pedrosousa.findwords.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TermoDTO {
    private String palavra;
    private OperadorLogicoEnum operadorAnterior;
}
