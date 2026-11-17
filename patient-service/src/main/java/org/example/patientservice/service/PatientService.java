package org.example.patientservice.service;
import org.example.patientservice.BillingServiceGrpcClient;
import org.example.patientservice.dto.PatientRequestDTO;
import org.example.patientservice.dto.PatientResponseDTO;
import org.example.patientservice.exception.EmailExistsException;
import org.example.patientservice.exception.PatientNotFoundException;
import org.example.patientservice.kafka.KafkaProducer;
import org.example.patientservice.mapper.PatientMapper;
import org.example.patientservice.model.Patient;
import org.example.patientservice.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class PatientService {
    private PatientRepository patientRepository;
    private final  BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;
    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer){
        this.patientRepository= patientRepository;
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.kafkaProducer=kafkaProducer;
    }
    public List<PatientResponseDTO> getPatients(){
        List<Patient> patients= patientRepository.findAll();
        List<PatientResponseDTO> patientResponseDTO = patients.stream()
                .map(patient -> PatientMapper.toDTO(patient)).toList();
        return patientResponseDTO;
    }
    public PatientResponseDTO getPatient(UUID id){
        Patient patient= patientRepository.findById(id)
                .orElseThrow(() -> new PatientNotFoundException("patient not found"+id));
                return PatientMapper.toDTO(patient);
    }
    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO){
        if (patientRepository.existsByEmail(patientRequestDTO.getEmail())){
            throw new EmailExistsException("Already exists " + patientRequestDTO.getEmail());
        }
        Patient newPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));//to server
        billingServiceGrpcClient.createBillingAccount(newPatient.getId().toString(),newPatient.getName(),newPatient.getEmail());
        kafkaProducer.sendEvent(newPatient);
        return PatientMapper.toDTO(newPatient); // to controller
    }
    public PatientResponseDTO updatePatient(UUID id,PatientRequestDTO patientRequestDTO){

        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("patient not found"+id));
        if (patientRepository.existsByEmailAndIdNot(patientRequestDTO.getEmail(),id)){
            throw new EmailExistsException("Already exists " + patientRequestDTO.getEmail());
        }
        patient.setName(patientRequestDTO.getName());
        patient.setAddress(patientRequestDTO.getAddress());
        patient.setEmail(patientRequestDTO.getEmail());
        patient.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        Patient updatedPatient  = patientRepository.save(patient);
        return  PatientMapper.toDTO(updatedPatient);
    }
    public void deletePatient(UUID id){
        patientRepository.deleteById(id);
    }

}
