package github.mess;

public class Transacao {
  private int valor;
  private char tipo;
  private String descricao;

  public Transacao() {}

  public Transacao(int valor, char tipo, String descricao) {
    this.valor = valor;
    this.tipo = tipo;
    this.descricao = descricao;
  }

  public int getValor() {
    return valor;
  }

  public void setValor(int valor) {
    this.valor = valor;
  }

  public char getTipo() {
    return tipo;
  }

  public void setTipo(char tipo) {
    this.tipo = tipo;
  }

  public String getDescricao() {
    return descricao;
  }

  public void setDescricao(String descricao) {
    this.descricao = descricao;
  }
}
