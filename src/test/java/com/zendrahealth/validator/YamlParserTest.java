package com.zendrahealth.validator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class YamlParserTest {

    @Test
    public void readFMEA() throws IOException {
        YamlParser yamlParser = new YamlParser();
        File fmeaFile = getFile("validator.yml");
        final YamlParser.Config config = yamlParser.parse(fmeaFile);
        assertNotNull(config);
    }


    @Test
    public void columnNameAMapsto0() throws IOException {
        assertEquals("A", YamlParser.columnName(0));
    }


    @Test
    public void columnNameDMapsto3() throws IOException {
        assertEquals("D", YamlParser.columnName(3));
    }

    @Test
    public void columnNameZMapsto25() throws IOException {
        assertEquals("Z", YamlParser.columnName(25));
    }

    @Test
    public void columnNameAAMapsto26() throws IOException {
        assertEquals("AA", YamlParser.columnName(26));
    }

    @Test
    public void columnNameAZMapsto51() throws IOException {
        assertEquals("AZ", YamlParser.columnName(51));
    }

    @Test
    public void columnNameBZMapsto77() throws IOException {
        assertEquals("BZ", YamlParser.columnName(77));
    }

    private File getFile(String filePath) {
        ClassLoader classLoader = MainTest.class.getClassLoader();
        return new File(classLoader.getResource(filePath).getFile());
    }
}
