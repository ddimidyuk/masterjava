package ru.javaops.masterjava.matrix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

/**
 * gkislin
 * 03.07.2016
 */
public class MatrixUtil {

    public static int[][] concurrentMultiply(int[][] matrixA, int[][] matrixB, ExecutorService executor) throws InterruptedException, ExecutionException {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int BT[][] = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }

        final CompletionService<LineResult> completionService = new ExecutorCompletionService<>(executor);
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < matrixSize; i++) {
            int finalI = i;
            futures.add(completionService.submit(() -> calculateLine(finalI, matrixA, BT)));
        }

        while (!futures.isEmpty()) {
            Future<LineResult> future = completionService.poll(10, TimeUnit.SECONDS);
            if (future == null) {
                throw new InterruptedException();
            }
            futures.remove(future);
            LineResult lineResult = future.get();
            System.arraycopy(lineResult.value, 0, matrixC[lineResult.line], 0, matrixSize);
        }
        return matrixC;
    }


    private static LineResult calculateLine(int lineIndex, int[][] matrixA, int[][] BT) {
        final int matrixSize = matrixA.length;
        final int[] lineResults = new int[matrixSize];
        for (int j = 0; j < matrixSize; j++) {
            int sum = 0;
            for (int k = 0; k < matrixSize; k++) {
                sum += matrixA[lineIndex][k] * BT[j][k];
            }
            lineResults[j] = sum;
        }
        return new LineResult(lineIndex, lineResults);
    }

    public static int[][] singleThreadMultiply(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        final int[][] matrixC = new int[matrixSize][matrixSize];

        int BT[][] = new int[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                BT[j][i] = matrixB[i][j];
            }
        }
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                int sum = 0;
                for (int k = 0; k < matrixSize; k++) {
                    sum += matrixA[i][k] * BT[j][k];
                }
                matrixC[i][j] = sum;
            }
        }
        return matrixC;
    }

    public static int[][] create(int size) {
        int[][] matrix = new int[size][size];
        Random rn = new Random();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = rn.nextInt(10);
            }
        }
        return matrix;
    }

    public static boolean compare(int[][] matrixA, int[][] matrixB) {
        final int matrixSize = matrixA.length;
        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                if (matrixA[i][j] != matrixB[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class LineResult {

        private int line;
        private int value[];

        public LineResult(int i, int value[]) {
            this.line = i;
            this.value = value;
        }

        @Override
        public String toString() {
            return "LineResult{" +
                    "line=" + line +
                    ", value=" + Arrays.toString(value) +
                    '}';
        }
    }
}
