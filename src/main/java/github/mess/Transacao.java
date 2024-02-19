package github.mess;

import com.dslplatform.json.CompiledJson;

@CompiledJson
public class Transacao {
  public int valor;
  public String tipo;
  public String descricao;

  public Transacao() {}

  public Transacao(final int valor, final String tipo, final String descricao) {
    this.valor = valor;
    this.tipo = tipo;
    this.descricao = descricao;
  }
}
