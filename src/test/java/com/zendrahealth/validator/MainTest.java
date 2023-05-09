package com.zendrahealth.validator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {


    @Test
    public void readFMEAInCorrectFormatHasNoErrorMessages() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA.xlsx");
        File yamlFile = getFile("validator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.isEmpty(), "No validation error messages should exist");
    }

    @Test
    public void readFMEAWithMissingFormulaAtN4MappingDisplaysErrorMessageOfMissingFormula() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_missing_formula_N4.xlsx");
        File yamlFile = getFile("validator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==1, "1 validation error messages should be exist");
        final YamlParser.Message message = messages.get(0);

        assertEquals(new YamlParser.Identifier(3, 13, "N"), message.identifier());
        assertEquals("Expected:Not Blank, Actual: Blank",message.message());

    }

    @Test
    public void readFMEAWithWrongFormulaAtN4MappingDisplaysErrorMessageOfWrongFormula() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_wrong_formula_N4.xlsx");
        File yamlFile = getFile("validator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==1, "1 validation error messages should be exist");
        final YamlParser.Message message = messages.get(0);

        assertEquals(new YamlParser.Identifier(4, 13, "N"), message.identifier());
        assertEquals("Expected:=IF(M5<16,IF(M5<8,1,2),3), Actual:IF(M5<16, IF(N5<8, 1, 2), 3)",message.message());

    }

    @Test
    public void readFMEAWithEmptyCellsForStringAndNumericFieldsDisplaysErrorMessage() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_missing_string_data_and_integer_data.xlsx");
        File yamlFile = getFile("validator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==11, "11 validation error messages should be exist");
        final YamlParser.Message message = messages.get(0);

        // Verify merged cells fields are empty
        for (int i = 1; i <=9; i++) {
            final YamlParser.Identifier identifier = new YamlParser.Identifier(i, 0, "A");
            assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Not Blank, Actual: Blank")));
        }

        // Verify integer cells fields are empty
        final YamlParser.Identifier identifier = new YamlParser.Identifier(5, 9, "J");
        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Not Blank, Actual: Blank")));
        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Integer, Actual: Non Integer")));
    }

    private File getFile(String filePath) {
        ClassLoader classLoader = MainTest.class.getClassLoader();
        return new File(classLoader.getResource(filePath).getFile());
    }
}
