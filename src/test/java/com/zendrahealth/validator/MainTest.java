package com.zendrahealth.validator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class MainTest {


    @Test
    public void readFMEA() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA.xlsx");
        File yamlFile = getFile("validator.yml");
        main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
    }

    private File getFile(String filePath) {
        ClassLoader classLoader = MainTest.class.getClassLoader();
        return new File(classLoader.getResource(filePath).getFile());
    }
}
