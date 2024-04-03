class Pagina {
    int numero;
    boolean bitR; // Representa si la página ha sido accedida recientemente.

    public Pagina(int numero) {
        this.numero = numero;
        this.bitR = true; // Inicialmente, asumimos que cada página cargada ha sido accedida.
    }
}
