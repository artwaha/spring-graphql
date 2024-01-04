package orci.or.tz.appointments.web.internal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import orci.or.tz.appointments.dto.doctor.DocDto;
import orci.or.tz.appointments.dto.doctor.DocExternalDto;
import orci.or.tz.appointments.dto.doctor.DoctorRequestDto;
import orci.or.tz.appointments.dto.doctor.DoctorUpdateDto;
import orci.or.tz.appointments.exceptions.*;
import orci.or.tz.appointments.models.Doctor;
import orci.or.tz.appointments.services.DoctorService;
import orci.or.tz.appointments.services.InayaService;
import orci.or.tz.appointments.utilities.Commons;
import orci.or.tz.appointments.utilities.GenericResponse;
import orci.or.tz.appointments.web.internal.api.Doctor2Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.validation.Valid;


@RestController
public class DoctorInternalController implements Doctor2Api {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private InayaService inayaService;

    @Autowired
    private Commons commons;

  

    @Override
    public ResponseEntity<List<JsonNode>> GetAllDoctorsFromInayaApi() 
        throws ResourceNotFoundException, OperationFailedException {
        try {
            List<JsonNode> resp = new ArrayList<>();
            String doctorsFromInaya = inayaService.GetAllSpecialists();
            if (doctorsFromInaya == null) {
                throw new ResourceNotFoundException("No doctors Fetched From Inaya");
            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(doctorsFromInaya);

                int codeValue = jsonNode.hasNonNull("code") ? jsonNode.get("code").asInt() : 0;
                if (codeValue == 200) {
                    JsonNode doctorsNode = jsonNode.get("data");
                    if (doctorsNode.isArray()) {
                        for (JsonNode doctor : doctorsNode) {
                            resp.add(doctor);
                        }
                        return ResponseEntity.ok(resp);
                    } else {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(resp); 
                    }
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(resp);
                }
            }
        } catch (Exception e) {
            // handle the exceptions
            System.out.println(e.getMessage());
            throw new OperationFailedException("Failed to Connect or retrieve data from Inaya API");
        }
    }


    @Override
    public ResponseEntity<GenericResponse<List<DocDto >>> GetAllDoctorsFromAppointmentDB(int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size);

    List<Doctor> doctors = doctorService.GetAllDoctors(pageRequest);

        List<DocDto > resp = new ArrayList<>();
        for (Doctor doctor : doctors) {
            DocDto dr = commons.GenerateDoctorInternalDto(doctor);
            resp.add(dr);
        }

        GenericResponse<List<DocDto>> response = new GenericResponse<>();
        response.setCurrentPage(page);
        response.setPageSize(size);
        Integer totalCount = doctorService.countTotalItems();
        response.setTotalItems(totalCount);
        response.setTotalPages(commons.GetTotalNumberOfPages(totalCount, size));
        response.setData(resp);

        return ResponseEntity.ok(response);

    }


    @Override
    public ResponseEntity<DocDto > createDoctorIntoAppointmentDB(@Valid @RequestBody DoctorRequestDto doctorRequestDto)
    throws ResourceNotFoundException, IOException {

        if (!doctorService.CheckIfDoctorExistsByInayaId(doctorRequestDto.getInayaId())) {
            String doctorNode = inayaService.GetSpecialistFromInayaById(doctorRequestDto.getInayaId());

            if (doctorNode == null) {
                throw new ResourceNotFoundException("The Specialist with The provided Id does not Exist in Inaya");
            }
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(doctorNode);
            int codeValue = jsonNode.hasNonNull("code") ? jsonNode.get("code").asInt() : 0;
        
            if (codeValue == 200) {
                String doctorName = jsonNode.get("data").get("fullName").asText();
                Doctor doctor = new Doctor();
                doctor.setDoctorName(doctorName);
                doctor.setInayaId(doctorRequestDto.getInayaId());
                doctor.setMonday(doctorRequestDto.isMonday());
                doctor.setTuesday(doctorRequestDto.isTuesday());
                doctor.setWednesday(doctorRequestDto.isWednesday());
                doctor.setThursday(doctorRequestDto.isThursday());
                doctor.setFriday(doctorRequestDto.isFriday());
                // save thye object in the DB
                doctorService.SaveDoctor(doctor);
                DocDto docExternalDto = commons.GenerateDoctorInternalDto(doctor);
                return ResponseEntity.ok(docExternalDto);
            } else {
                throw new ResourceNotFoundException("Doctor with Provided ID" + doctorRequestDto.getInayaId() + "Does Not Exists in Inaya");
            }
        
        }
        // we have to avoid duplication if the doctor with the specific ID from Inaya already exists
        throw new IOException("The Specialist With the Provded ID" + doctorRequestDto.getInayaId() + "already Exists");
    }

    @Override
    public ResponseEntity<DocDto > UpdateDoctorIntoAppointmentDB(@Valid @RequestBody DoctorUpdateDto doctorUpdateDto, Long id)
            throws ResourceNotFoundException {

                Optional<Doctor> d = doctorService.GetDoctorById(id);

                if (!d.isPresent()) {
            throw new ResourceNotFoundException("Directorate with provided ID [" + id + "] does not exist.");
        }

        Doctor dr = d.get();
        dr.setMonday(doctorUpdateDto.isMonday());
        dr.setTuesday(doctorUpdateDto.isTuesday());
        dr.setWednesday(doctorUpdateDto.isWednesday());
        dr.setThursday(doctorUpdateDto.isThursday());
        dr.setFriday(doctorUpdateDto.isFriday());

        doctorService.SaveDoctor(dr);

        DocDto resp = commons.GenerateDoctorInternalDto(dr);
        return ResponseEntity.ok(resp);

            }    


}
