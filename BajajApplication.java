package com.bajaj;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.http.*;
import org.springframework.web.client.*;
import org.springframework.context.annotation.*;
import java.util.*;

@SpringBootApplication
public class BajajApplication implements ApplicationRunner {
    
    public static void main(String[] args) {
        SpringApplication.run(BajajApplication.class, args);
    }
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("🚀 Starting Bajaj Finserv Submission");
        
        // Your details - CHANGE THESE
        String name = "YOUR_NAME";
        String regNo = "YOUR_REG_NO"; // Odd number for Question 1
        String email = "YOUR_EMAIL";
        
        // Step 1: Get webhook URL
        String webhookUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
        
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", name);
        requestBody.put("regNo", regNo);
        requestBody.put("email", email);
        
        RestTemplate restTemplate = new RestTemplate();
        
        try {
            // Generate webhook
            ResponseEntity<Map> response = restTemplate.postForEntity(
                webhookUrl, requestBody, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, String> responseBody = response.getBody();
                String submitUrl = responseBody.get("webhook");
                String token = responseBody.get("accessToken");
                
                // Step 2: Submit SQL query
                String sqlQuery = getFinalSqlQuery();
                
                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", token);
                headers.setContentType(MediaType.APPLICATION_JSON);
                
                Map<String, String> submission = new HashMap<>();
                submission.put("finalQuery", sqlQuery);
                
                HttpEntity<Map<String, String>> entity = new HttpEntity<>(submission, headers);
                
                ResponseEntity<String> finalResponse = restTemplate.postForEntity(
                    submitUrl, entity, String.class);
                
                System.out.println("✅ Submission Complete!");
                System.out.println("Status: " + finalResponse.getStatusCode());
                System.out.println("SQL Query Submitted:");
                System.out.println(sqlQuery);
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }
    
    private String getFinalSqlQuery() {
        return "SELECT d.DEPARTMENT_NAME, MAX(p.SALARY) as SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as EMPLOYEE_NAME, e.EMP_ID as EMPLOYEE_ID, e.EMP_ID as EMP_ID, TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) as AGE FROM DEPARTMENT d JOIN EMPLOYEE e ON d.DEPARTMENT_ID = e.DEPARTMENT JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID WHERE DAY(p.PAYMENT_TIME) != DAY(LAST_DAY(p.PAYMENT_TIME)) GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME, e.EMP_ID, e.FIRST_NAME, e.LAST_NAME, e.DOB HAVING MAX(p.SALARY) = (SELECT MAX(p2.SALARY) FROM PAYMENTS p2 JOIN EMPLOYEE e2 ON p2.EMP_ID = e2.EMP_ID WHERE e2.DEPARTMENT = e.DEPARTMENT AND DAY(p2.PAYMENT_TIME) != DAY(LAST_DAY(p2.PAYMENT_TIME))) ORDER BY d.DEPARTMENT_NAME;";
    }
}
