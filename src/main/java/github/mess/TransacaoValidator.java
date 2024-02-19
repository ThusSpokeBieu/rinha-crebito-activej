package github.mess;

import java.util.HashMap;
import java.util.Map;

public abstract class TransacaoValidator {

  private static final Map<String, String> tipoMapping = new HashMap<>();

  static {
    tipoMapping.put("c", "c");
    tipoMapping.put("d", "d");
  }

  public static boolean isValid(final Transacao transacao) {
    if (tipoMapping.get(transacao.tipo) == null
        || transacao.descricao == null
        || transacao.descricao.isBlank()
        || transacao.descricao.length() > 10) return false;
    return true;
  }
}
