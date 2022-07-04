package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

@Service
public class ImageMerger {
    private static final String RESOURCES_DIR = ImageMerger.class.getResource("/").getPath();
    private static final String NUMBERCARDS_DIR = ImageMerger.class.getResource("/numbercards/").getPath();
    private static final String GENERATED_DIR = RESOURCES_DIR + "generated/";
    private static final int IMAGE_WIDTH = 441;
    private static final int IMAGE_HEIGHT = 677;
    public static final String JPG = ".jpg";

    public ImageMerger() {
        initGeneratedFilesDirectory();
    }

    @SneakyThrows
    public File mergeImages(List<String> numbers) {
        int rowSize = numbers.size() == 4 ? 2 : 3;
        int rowCount = numbers.size() / rowSize;
        if (numbers.size() % rowSize != 0) {
            rowCount++;
        }

        int pageHeight = rowCount * IMAGE_HEIGHT;
        int pageWidth;
        if (rowCount == 1) {
            pageWidth = IMAGE_WIDTH * numbers.size();
        } else {
            pageWidth = IMAGE_WIDTH * rowSize;
        }
        BufferedImage page = new BufferedImage(pageWidth, pageHeight, BufferedImage.TYPE_INT_ARGB);
        var numbersIterator = numbers.iterator();
        for (int row = 0; row < rowCount; row++) {
            for (int column = 0; column < rowSize; column++) {
                if (numbersIterator.hasNext()) {
                    String number = numbersIterator.next();
                    File file = new File(NUMBERCARDS_DIR + number + JPG);

                    BufferedImage image = ImageIO.read(file);
                    int x = column * IMAGE_WIDTH;
                    int y = row * IMAGE_HEIGHT;
                    page.createGraphics().drawImage(image, x, y, null);
                }
            }
        }

        File resultFile = new File(GENERATED_DIR + "/generated.png");
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
