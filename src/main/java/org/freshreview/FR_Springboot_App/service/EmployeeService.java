package org.freshreview.FR_Springboot_App.service;
import org.apache.poi.ss.usermodel.*;
import org.freshreview.FR_Springboot_App.dto.Employee;
import org.freshreview.FR_Springboot_App.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    @Autowired
    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public void processExcelFile(InputStream excelFile) throws IOException {
        Set<String> uniquePhoneNumbers = new HashSet<>();

        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);

            Iterator<Row> iterator = sheet.iterator();

            while (iterator.hasNext()) {
                Row currentRow = iterator.next();
                Cell phoneCell = currentRow.getCell(6); 
                String phoneNumber = getCellValueAsString(phoneCell);

                if (uniquePhoneNumbers.contains(phoneNumber)) {
                    iterator.remove(); // Remove duplicate row
                } else {
                    uniquePhoneNumbers.add(phoneNumber);
                }
            }

            for (Row row : sheet) {
                String firstName = getCellValueAsString(row.getCell(1));
                String lastName = getCellValueAsString(row.getCell(2));

                boolean employeeExists = employeeRepository.findByFirstNameAndLastName(firstName, lastName).isPresent();

                String uniqueId = generateUniqueId(firstName, lastName, employeeExists);

                insertIntoDatabase(row, uniqueId);
            }
        }
    }

    private String generateUniqueId(String firstName, String lastName, boolean employeeExists) {
        String baseId = (firstName + lastName).toLowerCase();

        if (employeeExists) {
            int count = 1;
            String uniqueId = baseId + count;

            while (employeeRepository.existsByUniqueId(uniqueId)) {
                count++;
                uniqueId = baseId + count;
            }
            return uniqueId;
        } else {
            return baseId;
        }
    }

    private void insertIntoDatabase(Row row, String uniqueId) {
        int serialNumber = (int) row.getCell(0).getNumericCellValue();
        String firstName = getCellValueAsString(row.getCell(1));
        String lastName = getCellValueAsString(row.getCell(2));
        
        // Convert salary to int
        int salary = Integer.parseInt(getCellValueAsString(row.getCell(3)));

        String jobPosition = getCellValueAsString(row.getCell(4));

        // Convert phone number to long
        long phoneNumber = Long.parseLong(getCellValueAsString(row.getCell(5)));

        Employee employee = new Employee(null, serialNumber, firstName, lastName, salary, jobPosition, uniqueId, phoneNumber);
        employeeRepository.save(employee);
    }
    
//    Employee employee = new Employee(null, serialNumber, firstName, lastName, salary, jobPosition, uniqueId, phoneNumber);

    
    //Tried handling the Type mismatch between the excel sheet datatype and the database
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return ""; // Handle null cells appropriately
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return String.valueOf(cell.getDateCellValue());
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }
}

