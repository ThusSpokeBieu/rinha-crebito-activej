package github.mess;

import static io.activej.common.Checks.checkNotNull;
import static io.activej.json.JsonValidationUtils.validateNotNull;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import io.activej.json.JsonCodec;
import io.activej.json.JsonCodecs;
import java.io.IOException;

public class TransacaoCodec {
  private static final TipoCodec tipoCodec = new TipoCodec();
  private static final DescricaoCodec descricaoCodec = new DescricaoCodec();
  private static final JsonCodec<Integer> integerCodec = JsonCodecs.ofInteger();

  public static JsonCodec<Transacao> create() {
    return JsonCodecs.ofObject(
        Transacao::new,
        "valor",
        Transacao::getValor,
        integerCodec,
        "tipo",
        Transacao::getTipo,
        tipoCodec,
        "descricao",
        Transacao::getDescricao,
        descricaoCodec);
  }

  static class TipoCodec implements JsonCodec<Character> {

    @Override
    public Character read(JsonReader<?> reader) throws IOException {
      return switch (validateNotNull(reader.readString())) {
        case "c" -> 'c';
        case "d" -> 'd';
        default -> throw reader.newParseError("Tipo deve ser 'c' ou 'd' ");
      };
    }

    @Override
    public void write(JsonWriter writer, Character value) {
      writer.writeString(checkNotNull(value).toString());
    }
  }

  static class DescricaoCodec implements JsonCodec<String> {

    @Override
    public String read(JsonReader<?> reader) throws IOException {
      String result = validateNotNull(reader.readString());
      if (result.isBlank()) throw reader.newParseError("Descrição não pode ser vazia");
      if (result.length() > 10)
        throw reader.newParseError("Descrição não pode ser maior que 10 caracteres");
      return result;
    }

    @Override
    public void write(JsonWriter writer, String value) {
      writer.writeString(checkNotNull(value));
    }
  }
}
