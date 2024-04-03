public class ReferenciaPagina {
    int numeroPagina;
    String accion; // 'R' para lectura, 'W' para escritura

    public ReferenciaPagina(int numeroPagina, String accion) {
        this.numeroPagina = numeroPagina;
        this.accion = accion;
    }
}
