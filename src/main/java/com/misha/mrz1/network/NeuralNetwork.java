package com.misha.mrz1.network;

import com.misha.mrz1.service.Matrix;
import com.misha.mrz1.service.ImgRectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NeuralNetwork {
    private static final int RECTANGLE_WIDTH = 4;
    private static final int RECTANGLE_HEIGHT = 4;
    private final int p;
    private static final double LEARNING_RATE = 0.0001;
    private double error;
    private Matrix firstLayerMatrix;
    private Matrix secondLayerMatrix;

    public NeuralNetwork(double error) {
        p = RECTANGLE_HEIGHT * RECTANGLE_WIDTH * 3 / 2;
        this.error = error;
        createFirstLayerWeightsMatrix();
        createSecondLayerWeightsMatrix();
    }

    public List<ImgRectangle> compressImage(BufferedImage image) {
        List<ImgRectangle> input = splitImageIntoRectangles(image);
        Matrix vector;
        for (ImgRectangle rectangle : input) {
            vector = rectangle.getVector().multiply(firstLayerMatrix).get();
            rectangle.setVector(vector);
        }
        return input;
    }

    public BufferedImage restoreImage(List<ImgRectangle> compressed, int width, int height) {
        BufferedImage restoredImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Matrix vector;
        for (ImgRectangle rectangle : compressed) {
            vector = rectangle.getVector().multiply(secondLayerMatrix).get();
            int x = rectangle.getX();
            int y = rectangle.getY();
            List<Color> colors = ImgRectangle.convertVectorToColors(vector);
            int position = 0;
            for (int i = 0; i < RECTANGLE_WIDTH; i++) {
                for (int j = 0; j < RECTANGLE_HEIGHT; j++) {
                    Color color = colors.get(position++);
                    if (x + i < width) {
                        if (y + j < height) {
                            restoredImage.setRGB(x + i, y + j, color.getRGB());
                        }
                    }
                }
            }
        }
        return restoredImage;
    }

    public void learn(BufferedImage image) {
        List<ImgRectangle> rectangles = splitImageIntoRectangles(image);
        int iteration = 0;
        Matrix restoredVector;
        Matrix delay;
        Matrix inputVector;
        Matrix outputVector;
        double E = Double.MAX_VALUE;
        while (E > error) {
            E = 0;
            for (ImgRectangle rectangle : rectangles) {
                inputVector = rectangle.getVector();
                outputVector = inputVector.multiply(firstLayerMatrix).get();
                restoredVector = outputVector.multiply(secondLayerMatrix).get();
                delay = restoredVector.subtract(inputVector).get();
                correctWeights(inputVector, outputVector, delay);
                E += calculateError(delay);
            }
            iteration++;
            System.out.println("iteration- " + iteration + " error- " + E);
        }
    }

    public int getRectangleWidth() {
        return RECTANGLE_WIDTH;
    }

    public int getRectangleHeight() {
        return RECTANGLE_HEIGHT;
    }

    public int getP() {
        return p;
    }

    public double getLearningRate() {
        return LEARNING_RATE;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    private void createFirstLayerWeightsMatrix() {
        firstLayerMatrix = Matrix.randomMatrix(RECTANGLE_WIDTH * RECTANGLE_HEIGHT * 3, p);
    }

    private void createSecondLayerWeightsMatrix() {
        secondLayerMatrix = Matrix.randomMatrix(p, RECTANGLE_WIDTH * RECTANGLE_HEIGHT * 3);
    }

    private List<ImgRectangle> splitImageIntoRectangles(BufferedImage image) {
        List<ImgRectangle> rectangles = new ArrayList<>();
        int x = 0;
        while (x < image.getWidth()) {
            int y = 0;
            while (y < image.getHeight()) {
                List<Color> colors = new LinkedList<>();
                for (int i = x; i < x + RECTANGLE_WIDTH; i++) {
                    for (int j = y; j < y + RECTANGLE_HEIGHT; j++) {
                        if (i < image.getWidth() && j < image.getHeight()) {
                            Color color = new Color(image.getRGB(i, j));
                            colors.add(color);
                        } else {
                            colors.add(new Color(-1, -1, -1));
                        }
                    }
                }
                ImgRectangle rectangle = new ImgRectangle();
                rectangle.setColors(colors);
                rectangle.setX(x);
                rectangle.setY(y);
                rectangles.add(rectangle);
                y += RECTANGLE_HEIGHT;
            }
            x += RECTANGLE_WIDTH;
        }
        return rectangles;
    }

    private void correctWeights(Matrix inputVector, Matrix outputVector, Matrix delay) {
        firstLayerMatrix = firstLayerMatrix.subtract(inputVector.transpose().multiply(LEARNING_RATE).multiply(delay).get().multiply(secondLayerMatrix.transpose()).get()).get();
        secondLayerMatrix = secondLayerMatrix.subtract(outputVector.transpose().multiply(LEARNING_RATE).multiply(delay).get()).get();
        normaliseWeights(firstLayerMatrix);
        normaliseWeights(secondLayerMatrix);
    }

    private void normaliseWeights(Matrix matrix) {
        for (int i = 0; i < matrix.getRows(); i++) {
            double value = 0;
            for (int j = 0; j < matrix.getColumns(); j++) {
                value += matrix.getValue(i, j) * matrix.getValue(i, j);
            }
            value = Math.sqrt(value);
            for (int j = 0; j < matrix.getColumns(); j++) {
                double v = matrix.getValue(i, j) / value;
                matrix.setValue(v, i, j);
            }
        }
    }

    private double calculateError(Matrix vector) {
        double e = 0;
        for (int i = 0; i < vector.getColumns(); i++) {
            e += vector.getValue(0, i) * vector.getValue(0, i);
        }
        return e;
    }
}
