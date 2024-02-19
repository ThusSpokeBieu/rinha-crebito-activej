package github.mess;

import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonReader.BindObject;
import java.io.IOException;

public class TransacaoConverter {
  public static final JsonReader.BindObject<Transacao> JSON_BINDER =
      new BindObject<Transacao>() {
        public Transacao bind(JsonReader untypedReader, Transacao instance) throws IOException {
          try {
            JsonReader<Object> reader = untypedReader;
            if (instance == null) instance = new Transacao();
            reader.next(String.class);
            reader.semicolon();
            instance.valor = reader.next(int.class);
            reader.comma();
            reader.next(String.class);
            reader.semicolon();
            instance.tipo = reader.next(String.class);
            reader.comma();
            reader.next(String.class);
            reader.semicolon();
            instance.descricao = reader.next(String.class);
            return instance;
          } catch (Exception e) {
            throw e;
          }
        }
      };
}
