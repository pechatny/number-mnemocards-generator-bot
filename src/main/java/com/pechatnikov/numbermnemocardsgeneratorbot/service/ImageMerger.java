package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class ImageMerger {
    private static final String RESOURCES_DIR = ImageMerger.class.getResource("/").getPath();
    private static final String GENERATED_DIR = RESOURCES_DIR + "generated/";
    private static final int IMAGE_WIDTH = 441;
    private static final int IMAGE_HEIGHT = 677;
    private static final int ROW_SIZE = 3;

    public ImageMerger() {
        initGeneratedFilesDirectory();
    }

    @SneakyThrows
    public File mergeImages(List<String> numbers) {
        int rowCount = numbers.size() / ROW_SIZE;
        if (numbers.size() % ROW_SIZE != 0) {
            rowCount++;
        }

        int pageHeight = rowCount * IMAGE_HEIGHT;
        int pageWidth;
        if (rowCount == 1) {
            pageWidth = IMAGE_WIDTH * numbers.size();
        } else {
            pageWidth = IMAGE_WIDTH * ROW_SIZE;
        }
        BufferedImage page = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB);
        var numbersIterator = numbers.iterator();
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < ROW_SIZE; column++) {
                if (numbersIterator.hasNext()) {
                    String number = numbersIterator.next();
                    String path = "/numbercards/" + number + ".jpg";
                    File file = new File(getClass().getResource(path).getPath());
                    BufferedImage image = null;
                    try {
                        image = ImageIO.read(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    int x = column * IMAGE_WIDTH;
                    int y = row * IMAGE_HEIGHT;
                    page.createGraphics().drawImage(image, x, y, null);
                }
            }
        }

        File resultFile = new File(RESOURCES_DIR + "generated/generated.png");
        File directory = new File(RESOURCES_DIR + "generated/");
        if (!directory.exists()) {
            directory.mkdir();
        }
        ImageIO.write(page, "PNG", resultFile);

        return resultFile;
    }

    private void initGeneratedFilesDirectory() {
        File directory = new File(GENERATED_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }
}
