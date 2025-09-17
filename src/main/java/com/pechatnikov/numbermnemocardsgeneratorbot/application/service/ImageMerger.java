package com.pechatnikov.numbermnemocardsgeneratorbot.application.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ImageMerger {
    private static final String NUMBERCARDS_DIR = "numbercards/";
    private static final String GENERATED_DIR =  "generated/";
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
                    String imagePath = NUMBERCARDS_DIR + number + JPG;
                    log.debug("Путь к файлу: %s", imagePath );
                    Resource resource = new ClassPathResource(imagePath);

                    try (InputStream inputStream = resource.getInputStream()) {
                        BufferedImage image = ImageIO.read(inputStream);
                        int x = column * IMAGE_WIDTH;
                        int y = row * IMAGE_HEIGHT;
                        page.createGraphics().drawImage(image, x, y, null);
                    }
                }
            }
        }

        File resultFile = new File(GENERATED_DIR + "/" + UUID.randomUUID() + "-generated.png");
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
