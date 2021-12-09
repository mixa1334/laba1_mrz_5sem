package com.misha.mrz1;

import com.misha.mrz1.network.NeuralNetwork;
import com.misha.mrz1.service.ImgVector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class Main {
    private static final Logger logger = LogManager.getLogger();

    public static void main(String[] args) throws IOException {
        NeuralNetwork neuralNetwork = new NeuralNetwork(0.0001, 4, 4, 500);
        List<BufferedImage> images = inputImage();
        int L = 0;
        neuralNetwork.learn(images.get(3));
        for (int i = 1; i <= images.size(); i++) {
            BufferedImage image = images.get(i - 1);
            List<ImgVector> result = neuralNetwork.compressImage(image);
            L = result.size();
            BufferedImage restoredImage = neuralNetwork.restoreImage(result, image.getWidth(), image.getHeight());
            saveImage(restoredImage, "output_" + i);
        }
        printParameters(neuralNetwork, L);
    }

    private static List<BufferedImage> inputImage() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get("src/main/resources/images"))) {
            return paths.filter(Files::isRegularFile)
                    .map(img -> {
                        try {
                            return ImageIO.read(img.toFile());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }).toList();
        } catch (IOException e) {
            throw e;
        }
    }


    public static void printParameters(NeuralNetwork neuralNetwork, int L) {
        int N = neuralNetwork.getRectangleWidth() * neuralNetwork.getRectangleHeight() * 3;
        double Z = (N * L) / ((N + L) * neuralNetwork.getP() + 2.0);
        logger.log(Level.INFO, "n = " + neuralNetwork.getRectangleWidth());
        logger.log(Level.INFO, "m = " + neuralNetwork.getRectangleHeight());
        logger.log(Level.INFO, "p = " + neuralNetwork.getP());
        logger.log(Level.INFO, "Z = " + Z);
        logger.log(Level.INFO, "L = " + L);
    }

    private static void saveImage(BufferedImage image, String name) {
        File file = new File("src/main/resources/images/result/" + name + ".png");
        file.mkdirs();
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
