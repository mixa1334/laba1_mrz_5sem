package com.misha.mrz1;

import com.misha.mrz1.network.LinearRecirculationNetwork;
import com.misha.mrz1.network.ImageVector;
import com.misha.mrz1.service.ImageReader;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        LinearRecirculationNetwork neuralNetwork = new LinearRecirculationNetwork(0.0001, 4, 4, 500);
        List<Optional<BufferedImage>> images = ImageReader.readImage();
        int L = 0;
        neuralNetwork.learn(images.get(3).get());
        for (int i = 1; i <= images.size(); i++) {
            BufferedImage image = images.get(i - 1).get();
            List<ImageVector> result = neuralNetwork.compressImage(image);
            L = result.size();
            BufferedImage restoredImage = neuralNetwork.restoreImage(result, image.getWidth(), image.getHeight());
            ImageReader.writeImage(restoredImage, "output_" + i);
        }
        neuralNetwork.printParameters(L);
    }
}
