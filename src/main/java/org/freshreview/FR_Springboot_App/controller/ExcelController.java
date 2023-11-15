package org.freshreview.FR_Springboot_App.controller;

import org.freshreview.FR_Springboot_App.service.EmployeeService;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

@RestController
public class ExcelController {

    private final EmployeeService employeeService;

    @Autowired
    public ExcelController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processExcelFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please upload an Excel file", HttpStatus.BAD_REQUEST);
        }

        try {
            employeeService.processExcelFile(file.getInputStream());
            return ResponseEntity.ok("Excel file processed successfully");
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("Error processing the Excel file", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
