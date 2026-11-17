package org.example.patientservice;

import org.example.patientservice.repository.PatientRepository;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class PatientServiceApplication {

    public static void main(String[] args) {
     SpringApplication.run(PatientServiceApplication.class, args);
    }
}
