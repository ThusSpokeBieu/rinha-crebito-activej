package github.mess.codecs;

import github.mess.Transacao;
import io.activej.json.JsonCodec;
import io.activej.json.JsonCodecs;

public class TransacaoCodec {
  public static final JsonCodec<Transacao> CODEC = create();

  public static JsonCodec<Transacao> create() {
    return JsonCodecs.ofObject(
        Transacao::new,
        "valor",
        Transacao::valor,
        JsonCodecs.ofInteger(),
        "tipo",
        Transacao::tipo,
        JsonCodecs.ofString(),
        "descricao",
        Transacao::descricao,
        JsonCodecs.ofString());
  }
}
