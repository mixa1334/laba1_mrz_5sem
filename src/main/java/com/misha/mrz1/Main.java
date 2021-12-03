package com.misha.mrz1;

import com.misha.mrz1.network.NeuralNetwork;
import com.misha.mrz1.service.ImgRectangle;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        final double error = 500;
        NeuralNetwork neuralNetwork = new NeuralNetwork(error);
        List<BufferedImage> images = inputImage();
        if (images.size() < 1) {
            return;
        }
        neuralNetwork.learn(images.get(images.size() - 1));
        int L = 0;
        for (int i = 1; i <= images.size(); i++) {
            BufferedImage image = images.get(i - 1);
            List<ImgRectangle> result = neuralNetwork.compressImage(image);
            L = result.size();
            BufferedImage restoredImage = neuralNetwork.restoreImage(result, image.getWidth(), image.getHeight());
            saveImage(restoredImage, "output_" + i);
        }
        printParameters(neuralNetwork, L);
    }

    private static List<BufferedImage> inputImage() {
        List<BufferedImage> images = new LinkedList<>();
        JFrame frame = new JFrame();
        JFileChooser chosenFile = new JFileChooser("src/main/resources/images");
        chosenFile.setMultiSelectionEnabled(true);
        int ret = chosenFile.showOpenDialog(frame);
        if (ret == 0) {
            var files = chosenFile.getSelectedFiles();
            try {
                for (File file : files) {
                    images.add(ImageIO.read(file));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        frame.dispose();
        return images;
    }


    public static void printParameters(NeuralNetwork neuralNetwork, int L) {
        int N = neuralNetwork.getRectangleWidth() * neuralNetwork.getRectangleHeight() * 3;
        double Z = (N * L) / ((N + L) * neuralNetwork.getP() + 2.0);
        System.out.println("n = " + neuralNetwork.getRectangleWidth());
        System.out.println("m = " + neuralNetwork.getRectangleHeight());
        System.out.println("p = " + neuralNetwork.getP());
        System.out.println("Z = " + Z);
        System.out.println("L = " + L);
    }

    private static void saveImage(BufferedImage image, String name) {
        File file = new File("images/result/" + name + ".png");
        file.mkdirs();
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
