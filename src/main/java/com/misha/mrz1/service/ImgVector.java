package com.misha.mrz1.service;

import java.awt.*;
import java.util.LinkedList;
import java.util.List;

public class ImgVector {
    private static final double MAX_VALUE = 255;
    private int x;
    private int y;
    private Matrix vector;
    private List<Color> colors;

    public ImgVector() {
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Matrix getVector() {
        return vector;
    }

    public void setVector(Matrix vector) {
        this.vector = vector;
        colors = convertVectorToColors(vector);
    }

    public List<Color> getColors() {
        return colors;
    }

    public void setColors(List<Color> colors) {
        this.colors = colors;
        vector = convertColorsToVector(colors);
    }

    public static Matrix convertColorsToVector(List<Color> colors) {
        double[][] vector = new double[1][colors.size() * 3];
        for (int i = 0, j = 0; i < colors.size(); i++) {
            Color color = colors.get(i);
            vector[0][j++] = 2 * color.getRed() / MAX_VALUE - 1;
            vector[0][j++] = 2 * color.getGreen() / MAX_VALUE - 1;
            vector[0][j++] = 2 * color.getBlue() / MAX_VALUE - 1;
        }
        return new Matrix(vector);
    }

    public static List<Color> convertVectorToColors(Matrix vector) {
        List<Color> colors = new LinkedList<>();
        for (int i = 0; i < vector.getColumns(); ) {
            int red = convertValueToColor(vector.getValue(0, i++));
            int green = convertValueToColor(vector.getValue(0, i++));
            int blue = convertValueToColor(vector.getValue(0, i++));
            Color color = new Color(red, green, blue);
            colors.add(color);
        }
        return colors;
    }

    private static int convertValueToColor(double value) {
        double result = MAX_VALUE * (value + 1) / 2;
        if (result < 0) {
            result = 0;
        } else if (result > MAX_VALUE) {
            result = MAX_VALUE;
        }
        return (int) result;
    }
}