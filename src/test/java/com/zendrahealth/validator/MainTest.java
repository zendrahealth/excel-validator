package com.zendrahealth.validator;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.github.stefanbirkner.systemlambda.SystemLambda.catchSystemExit;
import static org.junit.jupiter.api.Assertions.*;

public class MainTest {


    @Test
    public void readInitialRiskAssessmentFMEAInCorrectFormatHasNoErrorMessages() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA.xlsx");
        File yamlFile = getFile("initialRiskAssessmentFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.isEmpty(), "No validation error messages should exist");
    }

    @Test
    public void readInitialRiskAssessmentFMEAWithMissingFormulaAtN4MappingDisplaysErrorMessageOfMissingFormula() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_missing_formula_N4.xlsx");
        File yamlFile = getFile("initialRiskAssessmentFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==2, "2 validation error messages should be exist");
        final YamlParser.Message message = messages.get(0);

        final YamlParser.Identifier identifier = new YamlParser.Identifier(4, "N");

        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Not Blank, Actual: Blank")));
        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:=IF(M4<16,IF(M4<8,1,2),3), Actual:Not a Formula but of type:BLANK")));

    }

    @Test
    public void readInitialRiskAssessmentFMEAStringAtFormulaColumnP10MappingDisplaysErrorMessageOfMissingFormula() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_no_formula_P10.xlsx");
        File yamlFile = getFile("initialRiskAssessmentFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==1, "1 Validation error messages should be exist");

        final YamlParser.Identifier identifier = new YamlParser.Identifier(10, "P");

        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:=INDEX('Scoring criteria'!$H$4:$L$6, MATCH(FMEA!N10, 'Scoring criteria'!$F$4:$F$6, 0), MATCH(FMEA!I10, 'Scoring criteria'!$H$3:$L$3, 0)), Actual:Not a Formula but of type:STRING")));

    }

    @Test
    public void readInitialRiskAssessmentFMEAWithWrongFormulaAtN4MappingDisplaysErrorMessageOfWrongFormula() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_wrong_formula_N4.xlsx");
        File yamlFile = getFile("initialRiskAssessmentFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==1, "1 validation error messages should be exist");
        final YamlParser.Message message = messages.get(0);

        assertEquals(new YamlParser.Identifier(5, "N"), message.identifier());
        assertEquals("Expected:=IF(M5<16,IF(M5<8,1,2),3), Actual:IF(M5<16, IF(N5<8, 1, 2), 3)",message.message());

    }

    @Test
    public void readInitialRiskAssessmentFMEAWithEmptyCellsForStringAndNumericFieldsDisplaysErrorMessage() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_missing_string_data_and_integer_data.xlsx");
        File yamlFile = getFile("initialRiskAssessmentFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.size()==11, "11 validation error messages should be exist");
        final YamlParser.Message message = messages.get(0);

        // Verify merged cells fields are empty
        for (int i = 1; i <=9; i++) {
            final YamlParser.Identifier identifier = new YamlParser.Identifier(i+1,  "A");
            assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Not Blank, Actual: Blank")));
        }

        // Verify integer cells fields are empty
        final YamlParser.Identifier identifier = new YamlParser.Identifier(6,  "J");
        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Not Blank, Actual: Blank")));
        assertTrue(messages.stream().anyMatch(m -> m.identifier().equals(identifier) && m.message().equals("Expected:Integer, Actual: Non Integer")));
    }


    @Test
    public void readPostMitigiationInCorrectFormatHasNoErrorMessages() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA.xlsx");
        File yamlFile = getFile("postRiskMitigationFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertTrue(messages.isEmpty(), "No validation error messages should exist");
    }

    @Test
    public void readPostMitigiationWrongFormulataInYHasErrorMessages() throws IOException {
        Main main = new Main();
        File fmeaFile = getFile("FMEA_wrong_formula_Y10.xlsx");
        File yamlFile = getFile("postRiskMitigationFMEAValidator.yml");
        final List<YamlParser.Message> messages = main.processFile(fmeaFile, Collections.singletonList(new YamlParser().parse(yamlFile)));
        assertFalse(messages.isEmpty(), "1 validation error messages should exist");

        final YamlParser.Message message = messages.get(0);
        assertEquals(new YamlParser.Identifier(10, "Y"), message.identifier());
        assertEquals("Expected:=INDEX('Scoring criteria'!$H$4:$L$6, MATCH(FMEA!W10, 'Scoring criteria'!$F$4:$F$6, 0), MATCH(FMEA!S10, 'Scoring criteria'!$H$3:$L$3, 0)), Actual:INDEX('Scoring criteria'!$H$4:$L$6, MATCH(FMEA!W10, 'Scoring criteria'!$F$4:$F$6, 0), MATCH(FMEA!R10, 'Scoring criteria'!$H$3:$L$3, 0))",message.message());
    }

    @Test
    public void mainMethodExecutionParamsWithErrorsThrowsSystemErrorExitCode2() throws Exception {
        File fmeaFile = getFile("FMEA_wrong_formula_Y10.xlsx");
        File yaml1File = getFile("initialRiskAssessmentFMEAValidator.yml");
        File yaml2File = getFile("postRiskMitigationFMEAValidator.yml");

        int statusCode = catchSystemExit(() -> {
            Main.main(new String[] {fmeaFile.getAbsolutePath(), yaml2File.getAbsolutePath(), yaml2File.getAbsolutePath()});
        });
        assertEquals(2, statusCode);
    }

    @Test
    public void mainMethodExecutionParamsWithErrorsThrowsNoSystemExit() throws Exception {
        File fmeaFile = getFile("FMEA.xlsx");
        File yaml1File = getFile("initialRiskAssessmentFMEAValidator.yml");
        File yaml2File = getFile("postRiskMitigationFMEAValidator.yml");

        Main.main(new String[] {fmeaFile.getAbsolutePath(), yaml2File.getAbsolutePath(), yaml2File.getAbsolutePath()});
    }

    private File getFile(String filePath) {
        ClassLoader classLoader = MainTest.class.getClassLoader();
        return new File(classLoader.getResource(filePath).getFile());
    }
}
