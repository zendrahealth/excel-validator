# Excel Validator

## Overview

As a medical device company, Zendra performs FMEA analysis for software as medical devices. The outputs of FMEA Analysis creates complicated Excel files with hundreds of risk assessment entries which became quite error prone (missing columns, wrong formula mapping).

Therefore, we built this tool to help validate that format of the Excel files.

Rulesets are defined within 'yaml' configuration file. Example content of a yaml configuration file is below.

```
--- # Sample yaml config file
validators:

  columns:
    A:
      - Type:
          type: string
          message: "Value should be string"
          notBlank: true
          mergeable: true
    C:
      - Type:
          type: string
          message: "Value should be string"
          notBlank: true
          mergeable: true
    D:
      - Type:
          type: string
          message: "Value should be string"
          notBlank: true
          mergeable: true
    E:
      - Type:
          type: string
          message: "Value should be string"
          notBlank: true
          mergeable: false

    F:
      - Type:
          type: formula
          message: "Value should be formula"
          formula: =VLOOKUP(E<rowIndex>,'Accidents, Hazards & Harms '!$B$2:$C$120,2,FALSE)
          notBlank: true
          mergeable: false
    G:
      - Type:
          type: integer
          message: "Value should be an integer"
          notBlank: true
          mergeable: false

```

## Instructions for use

 - Checkout this source code locally
 - Within the source folder location, execute the command`./gradlew build`  which generates the executable jar within `build/libs` folder
 - Execute command`java -jar build/libs/excel-validator-1.0-SNAPSHOT.jar src/test/resources/FMEA.xlsx src/test/resources/validator.yml` to perform validation.
   - Success message is returned if all ok, otherwise error message is returned along with the problem rows outputed to console
   - Multiple yaml configuration files files are supported `java -jar build/libs/excel-validator-1.0-SNAPSHOT.jar validator1.yml validator2.yml`

