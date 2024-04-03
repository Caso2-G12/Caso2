import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        List<Pagina> ram = Collections.synchronizedList(new ArrayList<>()); // Asegurar acceso sincronizado
        Thread actualizadorPaginas = new Thread(new ActualizadorPaginas(ram));
        actualizadorPaginas.start();

        while (true) {
            System.out.println("Seleccione una opción:");
            System.out.println("1. Generación de las referencias.");
            System.out.println("2. Calcular datos: número de fallas de página, porcentaje de hits, tiempos.");
            System.out.println("3. Salir.");

            int opcion = scanner.nextInt();

            switch (opcion) {
                case 1:
                    System.out.println("Configuración para la generación de referencias:");
                    // Solicitar parámetros necesarios al usuario
                    System.out.print("Ingrese el tamaño de página (TP): ");
                    int tp = scanner.nextInt();
                    System.out.print("Ingrese el número de filas (NF): ");
                    int nf = scanner.nextInt();
                    System.out.print("Ingrese el número de columnas (NC): ");
                    int nc = scanner.nextInt();
                    System.out.print("Ingrese el nombre del archivo donde se guardarán las referencias: ");
                    scanner.nextLine(); // Limpia el buffer
                    String nombreArchivo = scanner.nextLine();

                    generarArchivoDeReferencias(nombreArchivo, tp, nf, nc); // Tamaño del filtro fijo a 3
                    System.out.println("Archivo de referencias generado exitosamente.");
                    break;
                case 2:
                    calcularDatos();
                    break;
                case 3:
                    System.out.println("Saliendo...");
                    return;
                default:
                    System.out.println("Opción no reconocida, por favor intente de nuevo.");
                    break;
            }
        }
    }

    public static void generarArchivoDeReferencias(String nombreArchivo, int tp, int nf, int nc) {
        int tamFiltro = 3; // El tamaño del filtro es 3x3
        int tamElemento = 4; // Cada elemento (int) ocupa 4 bytes
        int elementosFiltro = tamFiltro * tamFiltro; // 9 elementos en el filtro
        int np = (int) Math.ceil((double) (elementosFiltro + nf * nc * 2) * tamElemento / tp);
        int nr = nf * nc * 3; // Cada elemento de M y R genera una referencia, cada elemento de F se usa nf*nc
                              // veces

        try (PrintWriter writer = new PrintWriter(nombreArchivo)) {
            writer.printf("TP=%d\nNF=%d\nNC=%d\nNF_NC_Filtro=%d\nNR=%d\nNP=%d\n", tp, nf, nc, tamFiltro, nr, np);

            int offset = 0; // Desplazamiento inicial para el filtro
            for (int i = 0; i < nf; i++) {
                for (int j = 0; j < nc; j++) {
                    // Referencias para la matriz de datos (M)
                    int paginaM = (elementosFiltro * tamElemento + (i * nc + j) * tamElemento) / tp;
                    int desplazamientoM = (elementosFiltro * tamElemento + (i * nc + j) * tamElemento) % tp;
                    writer.printf("M[%d][%d],%d,%d,R\n", i, j, paginaM, desplazamientoM);

                    // Usamos el filtro (F) para cada elemento de M, asumiendo reutilización
                    // completa del filtro
                    for (int fi = 0; fi < tamFiltro; fi++) {
                        for (int fj = 0; fj < tamFiltro; fj++) {
                            int paginaF = ((fi * tamFiltro + fj) * tamElemento) / tp;
                            int desplazamientoF = ((fi * tamFiltro + fj) * tamElemento) % tp;
                            writer.printf("F[%d][%d],%d,%d,R\n", fi, fj, paginaF, desplazamientoF);
                        }
                    }

                    // Referencias para la matriz de resultados (R), que sigue a M en memoria
                    int paginaR = ((elementosFiltro + nf * nc) * tamElemento + (i * nc + j) * tamElemento) / tp;
                    int desplazamientoR = ((elementosFiltro + nf * nc) * tamElemento + (i * nc + j) * tamElemento) % tp;
                    writer.printf("R[%d][%d],%d,%d,W\n", i, j, paginaR, desplazamientoR);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void calcularDatos() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Ingrese el nombre del archivo de referencias:");
        String nombreArchivo = scanner.next();

        System.out.println("Ingrese la capacidad de RAM (número de marcos de página):");
        int capacidadRAM = scanner.nextInt();

        SimuladorPaginacion simulador = new SimuladorPaginacion(capacidadRAM);
        Thread actualizadorBitR = new Thread(new ActualizadorBitR(simulador, 4000)); // Actualizar cada 4 segundos
        actualizadorBitR.start();

        try (Scanner fileScanner = new Scanner(new File(nombreArchivo))) {
            while (fileScanner.hasNextLine()) {
                String linea = fileScanner.nextLine();
                if (linea.startsWith("M") || linea.startsWith("F") || linea.startsWith("R")) {
                    // Suponiendo que cada línea de referencia sigue el formato: "M[0][0],2,4,R"
                    String[] partes = linea.split(",");
                    // Ignorando el identificador de la matriz y la posición por simplicidad
                    int numeroPagina = Integer.parseInt(partes[1]);
                    char tipoAcceso = partes[3].charAt(0); // 'R' para lectura, 'W' para escritura

                    simulador.procesarReferencia(numeroPagina, tipoAcceso);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("Archivo no encontrado: " + e.getMessage());
            return;
        }

        actualizadorBitR.interrupt();
        simulador.imprimirMetricas();
    }

}
