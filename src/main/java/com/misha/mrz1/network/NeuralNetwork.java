package com.misha.mrz1.network;

import com.misha.mrz1.service.Matrix;
import com.misha.mrz1.service.ImgRectangle;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NeuralNetwork {
    private final int rectangleWidth = 4;
    private final int rectangleHeight = 4;
    private final int p;
    private final double learningRate = 0.0001;
    private double error;
    private Matrix firstLayerMatrix;
    private Matrix secondLayerMatrix;

    public NeuralNetwork(double error) {
        p = rectangleHeight * rectangleWidth * 3 / 2;
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
            for (int i = 0; i < rectangleWidth; i++) {
                for (int j = 0; j < rectangleHeight; j++) {
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
        return rectangleWidth;
    }

    public int getRectangleHeight() {
        return rectangleHeight;
    }

    public int getP() {
        return p;
    }

    public double getLearningRate() {
        return learningRate;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    private void createFirstLayerWeightsMatrix() {
        firstLayerMatrix = Matrix.randomMatrix(rectangleWidth * rectangleHeight * 3, p);
    }

    private void createSecondLayerWeightsMatrix() {
        secondLayerMatrix = Matrix.randomMatrix(p, rectangleWidth * rectangleHeight * 3);
    }

    private List<ImgRectangle> splitImageIntoRectangles(BufferedImage image) {
        List<ImgRectangle> rectangles = new ArrayList<>();
        int x = 0;
        while (x < image.getWidth()) {
            int y = 0;
            while (y < image.getHeight()) {
                List<Color> colors = new LinkedList<>();
                for (int i = x; i < x + rectangleWidth; i++) {
                    for (int j = y; j < y + rectangleHeight; j++) {
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
                y += rectangleHeight;
            }
            x += rectangleWidth;
        }
        return rectangles;
    }

    private void correctWeights(Matrix inputVector, Matrix outputVector, Matrix delay) {
        firstLayerMatrix = firstLayerMatrix.subtract(inputVector.transpose().multiply(learningRate).multiply(delay).get().multiply(secondLayerMatrix.transpose()).get()).get();
        secondLayerMatrix = secondLayerMatrix.subtract(outputVector.transpose().multiply(learningRate).multiply(delay).get()).get();
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
