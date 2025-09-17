package com.pechatnikov.numbermnemocardsgeneratorbot.service;

import com.pechatnikov.numbermnemocardsgeneratorbot.application.service.ImageMerger;
import org.junit.jupiter.api.Test;

import java.util.List;

class ImageMergerTest {

    @Test
    void mergeImages() {
        var imageMerger = new ImageMerger();
        imageMerger.mergeImages(List.of("234", "123", "444", "233", "12"));
//        imageMerger.mergeImages(List.of("233", "12"));
    }
}