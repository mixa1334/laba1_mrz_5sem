package com.misha.mrz1.service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ImageReader {
    public static List<Optional<BufferedImage>> readImage() {
        List<Optional<BufferedImage>> result = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get("src/main/resources/images"))) {
            result = paths.filter(Files::isRegularFile).map(img -> {
                Optional<BufferedImage> image = Optional.empty();
                try {
                    image = Optional.of(ImageIO.read(img.toFile()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }).toList();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void writeImage(BufferedImage image, String name) {
        File file = new File("src/main/resources/images/result/" + name + ".png");
        file.mkdirs();
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
