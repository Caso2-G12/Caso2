import java.util.*;

public class SimuladorPaginacion {
    private List<Pagina> ram;
    private final int capacidadRAM;
    private long tiempoTotalAcceso = 0;
    private int fallosDePagina = 0;
    private int totalAccesos = 0; // Añadido para contar el total de accesos (hits + misses)

    public SimuladorPaginacion(int capacidadRAM) {
        this.capacidadRAM = capacidadRAM;
        this.ram = new ArrayList<>(capacidadRAM);
    }

    public synchronized void procesarReferencia(int numeroPagina, char tipoAcceso) {
        totalAccesos++; // Incrementar el total de accesos con cada referencia procesada
        Optional<Pagina> paginaOpt = ram.stream().filter(p -> p.numero == numeroPagina).findFirst();
        if (paginaOpt.isPresent()) {
            // Hit
            tiempoTotalAcceso += 30; // Acceso en ns
            Pagina pagina = paginaOpt.get();
            pagina.bitR = true; // Marcar la página como recientemente accedida.
        } else {
            // Miss
            fallosDePagina++;
            tiempoTotalAcceso += 10_000_000_000L; // 10 ms en ns para fallo de página

            if (ram.size() >= capacidadRAM) {
                reemplazarPagina();
            }
            ram.add(new Pagina(numeroPagina));
        }
    }

    private void reemplazarPagina() {
        Pagina paginaParaReemplazar = ram.stream().filter(p -> !p.bitR).findFirst().orElse(null);
        if (paginaParaReemplazar != null) {
            ram.remove(paginaParaReemplazar);
        } else {
            ram.forEach(p -> p.bitR = false);
            ram.remove(0);
        }
    }

    public synchronized List<Pagina> getRam() {
        return ram;
    }

    public void imprimirMetricas() {
        int hits = totalAccesos - fallosDePagina; // Calcula los hits como el total de accesos menos los fallos
        double porcentajeHits = (double) hits / totalAccesos * 100;

        System.out.println("Total de referencias: " + totalAccesos);
        System.out.println("Fallos de página: " + fallosDePagina);
        System.out.println("Hits: " + hits);
        System.out.println("Porcentaje de hits: " + String.format("%.2f", porcentajeHits) + "%");
        System.out.println("Tiempo total de acceso: " + tiempoTotalAcceso + " ns");
    }
}
