package com.zendrahealth.validator;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class YamlParser {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public record Config(String header, Validators validators) {}

    public record Validators (Columns columns) {
        public List<TypeHolder> getTypesForColumn(int columnIndex) throws NoSuchFieldException, IllegalAccessException {

            final RecordComponent[] recordComponents = columns.getClass().getRecordComponents();
            Field field = columns.getClass().getDeclaredField(recordComponents[columnIndex].getAccessor().getName());
            field.setAccessible(true);

            return (List<TypeHolder>) field.get(columns);

        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Columns(@JsonProperty("A") List<TypeHolder> A,
                          @JsonProperty("B") List<TypeHolder> B,
                          @JsonProperty("C") List<TypeHolder> C,
                          @JsonProperty("D") List<TypeHolder> D,
                          @JsonProperty("E") List<TypeHolder> E,
                          @JsonProperty("F") List<TypeHolder> F,
                          @JsonProperty("G") List<TypeHolder> G,
                          @JsonProperty("H") List<TypeHolder> H,
                          @JsonProperty("I") List<TypeHolder> I,
                          @JsonProperty("J") List<TypeHolder> J,
                          @JsonProperty("K") List<TypeHolder> K,
                          @JsonProperty("L") List<TypeHolder> L,
                          @JsonProperty("M") List<TypeHolder> M,
                          @JsonProperty("N") List<TypeHolder> N,
                          @JsonProperty("O") List<TypeHolder> O,
                          @JsonProperty("P") List<TypeHolder> P,
                          @JsonProperty("Q") List<TypeHolder> Q,
                          @JsonProperty("R") List<TypeHolder> R,
                          @JsonProperty("S") List<TypeHolder> S,
                          @JsonProperty("T") List<TypeHolder> T,
                          @JsonProperty("U") List<TypeHolder> U,
                          @JsonProperty("V") List<TypeHolder> V,
                          @JsonProperty("W") List<TypeHolder> W,
                          @JsonProperty("X") List<TypeHolder> X,
                          @JsonProperty("Y") List<TypeHolder> Y,
                          @JsonProperty("Z") List<TypeHolder> Z,
                          @JsonProperty("AA") List<TypeHolder> AA) {}

    private static char getCharacter(int index) {
        if (index < 0) {
            return 'A';
        }
        return (char) ('A'+index);
    }

    public static String columnName(int colIndex) {

        int numIterations = (colIndex + 1) / 26;
        int remainder = (colIndex + 1) % 26;

        if (numIterations > 9) {
            throw new IllegalStateException("Excel column size is too large, please revisit");
        }

        List<Character> suffix = new ArrayList<>();

        if ((numIterations == 1 && remainder != 0) || numIterations > 1) {
            suffix.add(getCharacter(remainder == 0 ? ((numIterations - 1) - 1) : numIterations - 1));
            colIndex = remainder == 0 ? 25 : remainder - 1;
        }
        suffix.add(getCharacter(colIndex));
        return suffix.stream().map(String::valueOf)
                .collect(Collectors.joining());

    }

    public record Identifier(int rowNumber, int cellNumber, String cellColumn) {
        @Override
        public String toString() {
            return "Identifier{" +
                    "rowNumber=" + rowNumber +
                    ", cellNumber=" + cellNumber +
                    ", cellColumn='" + cellColumn + '\'' +
                    '}';
        }
    }

    public record Message(Identifier identifier, Type type, String message) {

        @Override
        public String toString() {
            return "Message{" +
                    "identifier=" + identifier +
                    ", type=" + type +
                    ", message='" + message + '\'' +
                    '}';
        }
    }


    public record TypeHolder(@JsonProperty("Type") Type type) {

        public List<Message> validate(Cell cell) {
            final Identifier identifier = new Identifier(cell.getRowIndex(), cell.getColumnIndex(), columnName(cell.getColumnIndex()));
            List<Message> messages = new ArrayList<>();

            if (Boolean.TRUE.equals(type.notBlank) &&
                    cell.getCellType().equals(CellType.BLANK) &&
                    (!type().mergeable() || getIndexIfCellIsInMergedCells(cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex()).isEmpty())) {
                messages.add(
                        new Message(identifier, this.type, "Expected:Not Blank, Actual: Blank")
                );
            }

            if (type.type == TypeEnum.integer && cell.getCellType() != CellType.NUMERIC ||
                    (cell.getCellType() == CellType.NUMERIC && !isNumeric(cell))) {
                messages.add(
                        new Message(identifier, this.type, "Expected:Integer, Actual: Non Integer "));
            }


            if (type.type == TypeEnum.formula && cell.getCellType() == CellType.FORMULA) {
                final String substitutedFormula = type.formula
                                    .replace("(^=)","")
                                    .replace("<rowIndex>", (cell.getRowIndex() + 1) + "")
                                    .replace("<cellIndex>", (cell.getColumnIndex() + 1) + "");

                final String expected = substitutedFormula;
                final String actual = cell.getCellFormula().trim();

                // actual may not have leading '=' so do a contain instead
                if (!expected.contains(actual)) {
                    messages.add(
                            new Message(identifier, this.type, String.format("Expected:%s, Actual:%s", expected, actual))
                    );
                }
            }

            return messages;
        }

        private boolean isNumeric(Cell cell) {
            try {
                cell.getNumericCellValue();
                return true;
            } catch (IllegalStateException | NumberFormatException ne) {
                return false;
            }
        }

        private Optional<Integer> getIndexIfCellIsInMergedCells(Sheet sheet, int row, int column) {
            int numberOfMergedRegions = sheet.getNumMergedRegions();

            for (int i = 0; i < numberOfMergedRegions; i++) {
                CellRangeAddress mergedCell = sheet.getMergedRegion(i);

                if (mergedCell.isInRange(row, column)) {
                    return Optional.of(i);
                }
            }

            return Optional.empty();
        }
    }


    public record Type(TypeEnum type, String message, Boolean notBlank, Boolean mergeable, String formula) {
        @Override
        public String toString() {
            return "Type{" +
                    "type=" + type +
                    '}';
        }
    }

    public enum TypeEnum {
        string,
        formula,
        integer;
    }
    public Config parse(File file) throws IOException {
        return yamlMapper.readValue(file, Config.class);
    }
}
