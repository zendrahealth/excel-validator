package com.zendrahealth.validator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Main {

    private static final Logger logger = LogManager.getLogger(Main.class);

    public List<YamlParser.Message> processFile(File file, List<YamlParser.Config> configs)  {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);
            final List<YamlParser.Message> messages = new ArrayList<>();

            for (YamlParser.Config config: configs) {
                for (Row row : sheet) {

                    if (row.getRowNum() == 0) {
                        String firstHeaderName = config.header();
                        final Cell cell1 = row.getCell(row.getFirstCellNum());
                        if (cell1.getCellType() == CellType.STRING && cell1.getStringCellValue().trim().equalsIgnoreCase(firstHeaderName)) {
                            continue;
                        }
                    }
                    Iterator<Cell> cellIterator = row.cellIterator();
                    while (cellIterator.hasNext()) {
                        Cell cell = cellIterator.next();
                        List<YamlParser.TypeHolder> types = config.validators().getTypesForColumn(cell.getColumnIndex());

                        if (types == null) {
                            logger.debug("Stopping at identifier  Row Index:{}  Col:{}", (cell.getRowIndex() + 1), YamlParser.columnName(cell.getColumnIndex() + 1));
                            break;
                        }
                        types.forEach(type -> messages.addAll(type.validate(cell)));
                    }
                }
            }
            return messages;
        } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
            logger.error("Error", e);
            System.exit(2);
        }
        return Collections.emptyList();
    }


    public static void main(String[] args)  {
        String errorString = "Please pass at least two arguments; an excel file as first parameter and yaml rule mappings config files for other arguments: <excelFilePath> <yamlConfigFile1Path> <yamlConfigFile2Path> ..";
        if (1 < args.length) {
            try {
                File inFile = new File(args[0]);

                List<YamlParser.Config> configs = new ArrayList<>();
                for (int i = 1; i< args.length; i ++) {
                    File yamlFile = new File(args[1]);
                    configs.add(new YamlParser().parse(yamlFile));
                }
                Main main = new Main();
                final List<YamlParser.Message> messages = main.processFile(inFile, configs);

                if (messages.isEmpty()) {
                    Main.logger.info("Success - no validation errors");
                } else {
                    Main.logger.info("Failure - see validation errors below");
                    Main.logger.info("-------------------------------------");
                    messages.forEach(logger::info);
                    System.exit(2);
                }
            } catch (Exception e) {
                Main.logger.error(errorString, e);
                System.exit(2);
            }


        } else {
            Main.logger.error(errorString);
            System.exit(2);
        }
    }

}
