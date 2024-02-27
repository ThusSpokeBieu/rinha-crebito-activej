package github.mess;

import java.util.HashMap;
import java.util.Map;

public record Transacao(int valor, String tipo, String descricao) {

  private static final Map<String, String> tipoMapping = new HashMap<>();

  static {
    tipoMapping.put("c", "c");
    tipoMapping.put("d", "d");
  }

  public boolean isValid() {
    if (tipoMapping.get(this.tipo) == null || this.descricao == null || this.descricao.isBlank()
        || this.descricao.length() > 10)
      return false;
    return true;
  }

}
