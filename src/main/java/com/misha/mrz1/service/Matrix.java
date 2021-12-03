package com.misha.mrz1.service;

import java.util.Optional;

public class Matrix {
    private final int rows;
    private final int columns;
    private final double[][] matrix;

    public static Matrix randomMatrix(int rows, int columns) {
        double[][] randomWeights = new double[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                randomWeights[row][column] = Math.random() * 2 - 1;
            }
        }
        return new Matrix(randomWeights);
    }

    public Matrix(double[][] matrix) {
        rows = matrix.length;
        columns = matrix[0].length;
        for (int row = 0; row < rows; row++) {
            if (matrix[row].length != columns) {
                throw new IllegalArgumentException();
            }
        }
        this.matrix = matrix;
    }

    public Matrix transpose() {
        double[][] newMatrix = new double[columns][rows];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                newMatrix[column][row] = matrix[row][column];
            }
        }
        return new Matrix(newMatrix);
    }

    public Optional<Matrix> subtract(Matrix secondMatrix) {
        if (!checkMatrixSize(secondMatrix)) {
            return Optional.empty();
        }

        double[][] newMatrix = new double[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                newMatrix[row][column] = matrix[row][column] - secondMatrix.matrix[row][column];
            }
        }
        return Optional.of(new Matrix(newMatrix));
    }

    public Matrix multiply(double num) {
        double[][] newMatrix = new double[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                newMatrix[row][column] = num * matrix[row][column];
            }
        }
        return new Matrix(newMatrix);
    }

    public Optional<Matrix> multiply(Matrix secondMatrix) {
        if (secondMatrix.rows != columns) {
            return Optional.empty();
        }

        double[][] resultMatrix = new double[rows][secondMatrix.columns];
        for (int row1 = 0; row1 < rows; row1++) {
            for (int column2 = 0; column2 < secondMatrix.columns; column2++) {
                for (int column1 = 0; column1 < columns; column1++) {
                    resultMatrix[row1][column2] += matrix[row1][column1] * secondMatrix.matrix[column1][column2];
                }
            }
        }
        return Optional.of(new Matrix(resultMatrix));
    }

    public boolean checkMatrixSize(Matrix matrix) {
        return matrix.rows == rows && matrix.columns == columns;
    }


    public double getValue(int row, int column) {
        if (row >= rows || column >= columns) {
            throw new IllegalArgumentException();
        }

        return matrix[row][column];
    }

    public void setValue(double value, int row, int column) {
        if (row >= rows || column >= columns) {
            throw new IllegalArgumentException();
        }
        matrix[row][column] = value;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }
}
