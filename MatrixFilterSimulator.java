import java.io.PrintWriter;
import java.io.FileNotFoundException;

public class MatrixFilterSimulator {
    private int pageSize;
    private int[][] dataMatrix;
    private int[][] filter = { { 1, 1, 1 }, { 1, 1, 1 }, { 1, 1, 1 } }; // Ejemplo de filtro simple
    private String fileName;

    public MatrixFilterSimulator(int pageSize, int numRows, int numCols, String fileName) {
        this.pageSize = pageSize;
        this.dataMatrix = new int[numRows][numCols];
        // Inicialización simple de la matriz de datos para el ejemplo
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                dataMatrix[i][j] = (i + j) % 255; // Valores de ejemplo
            }
        }
        this.fileName = fileName;
    }

    public void generateReferenceFile() {
        try {
            PrintWriter writer = new PrintWriter(this.fileName);
            int numRows = dataMatrix.length;
            int numCols = dataMatrix[0].length;
            int NR = 0; // Inicializar contador de referencias

            writer.println("TP=" + this.pageSize);
            writer.println("NF=" + numRows);
            writer.println("NC=" + numCols);

            for (int i = 1; i < numRows - 1; i++) {
                for (int j = 1; j < numCols - 1; j++) {
                    int sum = 0;
                    for (int fi = -1; fi <= 1; fi++) {
                        for (int fj = -1; fj <= 1; fj++) {
                            int value = dataMatrix[i + fi][j + fj];
                            sum += filter[fi + 1][fj + 1] * value;
                            // Calcular y escribir referencia para lectura
                            int pos = ((i + fi) * numCols) + (j + fj);
                            writeReference(writer, pos, "R", numRows, numCols);
                            NR++;
                        }
                    }
                    // Calcular y escribir referencia para escritura (sum)
                    writeReference(writer, i * numCols + j, "W", numRows, numCols);
                    NR++;
                }
            }

            int NP = ((numRows * numCols) * 4) / pageSize + 1;
            // Reescribir el archivo con NR y NP correctos
            writer.println("NR=" + NR);
            writer.println("NP=" + NP);

            writer.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error al crear el archivo de referencias: " + e.getMessage());
        }
    }

    private void writeReference(PrintWriter writer, int pos, String action, int numRows, int numCols) {
        int pageNum = (pos * 4) / pageSize;
        int offset = (pos * 4) % pageSize;
        writer.println("Página: " + pageNum + ", Desplazamiento: " + offset + ", Acción: " + action);
    }
}
