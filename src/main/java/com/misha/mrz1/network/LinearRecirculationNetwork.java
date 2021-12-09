package com.misha.mrz1.network;

import com.misha.mrz1.service.Matrix;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LinearRecirculationNetwork {
    private static final Logger logger = LogManager.getLogger();
    private final int RECTANGLE_WIDTH;
    private final int RECTANGLE_HEIGHT;
    private final int p;
    private final double LEARNING_RATE;
    private final double error;
    private Matrix firstLayerMatrix;
    private Matrix secondLayerMatrix;

    public LinearRecirculationNetwork(double learningRate, int rectangleWidth, int rectangleHeight, double error) {
        LEARNING_RATE = learningRate;
        RECTANGLE_HEIGHT = rectangleHeight;
        RECTANGLE_WIDTH = rectangleWidth;
        p = RECTANGLE_HEIGHT * RECTANGLE_WIDTH * 3 / 2;
        this.error = error;
        firstLayerMatrix = Matrix.randomMatrix(RECTANGLE_WIDTH * RECTANGLE_HEIGHT * 3, p);
        secondLayerMatrix = Matrix.randomMatrix(p, RECTANGLE_WIDTH * RECTANGLE_HEIGHT * 3);
    }

    public List<ImageVector> compressImage(BufferedImage image) {
        List<ImageVector> input = splitImageIntoRectangles(image);
        Matrix vector;
        for (ImageVector rectangle : input) {
            vector = rectangle.getVector().multiply(firstLayerMatrix).get();
            rectangle.setVector(vector);
        }
        return input;
    }

    public BufferedImage restoreImage(List<ImageVector> compressed, int width, int height) {
        BufferedImage restoredImage = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        Matrix vector;
        for (ImageVector rectangle : compressed) {
            vector = rectangle.getVector().multiply(secondLayerMatrix).get();
            int x = rectangle.getX();
            int y = rectangle.getY();
            List<Color> colors = ImageVector.convertVectorToColors(vector);
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
        List<ImageVector> rectangles = splitImageIntoRectangles(image);
        int iteration = 0;
        Matrix restoredVector;
        Matrix difference;
        Matrix inputVector;
        Matrix outputVector;
        double E = Double.MAX_VALUE;
        while (E > error) {
            E = 0;
            for (ImageVector rectangle : rectangles) {
                inputVector = rectangle.getVector();
                outputVector = inputVector.multiply(firstLayerMatrix).get();
                restoredVector = outputVector.multiply(secondLayerMatrix).get();
                difference = restoredVector.subtract(inputVector).get();
                correctWeights(inputVector, outputVector, difference);
                E += calculateError(difference);
            }
            iteration++;
            logger.log(Level.INFO, "iteration- " + iteration + " error- " + E);
        }
    }

    public void printParameters(int L) {
        int N = RECTANGLE_WIDTH * RECTANGLE_HEIGHT * 3;
        double Z = (N * L) / ((N + L) * p + 2.0);
        logger.log(Level.INFO, "n = " + RECTANGLE_WIDTH);
        logger.log(Level.INFO, "m = " + RECTANGLE_HEIGHT);
        logger.log(Level.INFO, "p = " + p);
        logger.log(Level.INFO, "Z = " + Z);
        logger.log(Level.INFO, "L = " + L);
    }

    private List<ImageVector> splitImageIntoRectangles(BufferedImage image) {
        List<ImageVector> rectangles = new ArrayList<>();
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
                            colors.add(new Color(0, 0, 0));
                        }
                    }
                }
                ImageVector rectangle = new ImageVector();
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
        secondLayerMatrix = secondLayerMatrix.subtract(outputVector.transpose().multiply(LEARNING_RATE).multiply(delay).get()).get();
        firstLayerMatrix = firstLayerMatrix.subtract(inputVector.transpose().multiply(LEARNING_RATE).multiply(delay).get().multiply(secondLayerMatrix.transpose()).get()).get();
        normaliseWeights(firstLayerMatrix);
        normaliseWeights(secondLayerMatrix);
    }

    private void normaliseWeights(Matrix matrix) {
        for (int i = 0; i < matrix.getRows(); i++) {
            double value = 0;
            for (int j = 0; j < matrix.getColumns(); j++) {
                value += Math.pow(matrix.getValue(i, j), 2);
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
