import java.util.List;

class ActualizadorPaginas implements Runnable {
    private final List<Pagina> ram; // Estructura compartida

    public ActualizadorPaginas(List<Pagina> ram) {
        this.ram = ram;
    }

    @Override
    public void run() {
        while (true) {
            // Aquí implementarías la lógica para manejar una nueva referencia de página,
            // incluyendo añadir páginas a RAM o simular la carga desde SWAP.
            // Por simplicidad, este código está omitido.

            try {
                Thread.sleep(1); // Dormir 1 ms entre cada actualización
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
