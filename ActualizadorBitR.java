import java.util.List;

public class ActualizadorBitR implements Runnable {
    private SimuladorPaginacion simulador;
    private final long intervalo; // Milisegundos

    public ActualizadorBitR(SimuladorPaginacion simulador, long intervalo) {
        this.simulador = simulador;
        this.intervalo = intervalo;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                synchronized (simulador) {
                    List<Pagina> ram = simulador.getRam();
                    for (Pagina pagina : ram) {
                        pagina.bitR = false; // Resetea el bit R de cada página.
                    }
                }
                Thread.sleep(intervalo);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Asegura una terminación limpia del hilo.
        }
    }
}
