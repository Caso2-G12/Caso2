import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

class ActualizadorPaginas implements Runnable {
    private final List<Pagina> ram;
    private final Queue<ReferenciaPagina> colaReferencias;
    private final int capacidadRAM; // Capacidad máxima de RAM en páginas.

    public ActualizadorPaginas(List<Pagina> ram, Queue<ReferenciaPagina> colaReferencias, int capacidadRAM) {
        this.ram = ram;
        this.colaReferencias = colaReferencias;
        this.capacidadRAM = capacidadRAM;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() || !colaReferencias.isEmpty()) {
            ReferenciaPagina referencia = colaReferencias.poll();
            if (referencia != null) {
                procesarReferencia(referencia);
            }

            try {
                Thread.sleep(1); // Pequeña pausa para simular el tiempo de procesamiento.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void procesarReferencia(ReferenciaPagina referencia) {
        synchronized (ram) {
            Optional<Pagina> paginaOpt = ram.stream().filter(p -> p.numero == referencia.numeroPagina).findFirst();
            if (!paginaOpt.isPresent()) {
                // Fallo de página: La página no está en RAM.
                if (ram.size() >= capacidadRAM) {
                    // Necesitamos reemplazar una página.
                    reemplazarPagina();
                }
                // Simula la carga de la página en RAM.
                ram.add(new Pagina(referencia.numeroPagina));
                System.out.println("Cargando página " + referencia.numeroPagina + " en RAM.");
            }
            // Aquí podría ir la lógica para ajustar el bit R, etc.
        }
    }

    private void reemplazarPagina() {
        // Política de reemplazo simplificada: elimina la primera página sin el bit R
        // activo o la más antigua.
        for (Iterator<Pagina> iterator = ram.iterator(); iterator.hasNext();) {
            Pagina pagina = iterator.next();
            if (!pagina.getBitR()) {
                iterator.remove();
                return;
            }
            pagina.setBitR(false); // Resetea el bit R para la próxima ronda.
        }
        ram.remove(0); // Si todas tienen el bit R activo, elimina la más antigua.
    }
}
