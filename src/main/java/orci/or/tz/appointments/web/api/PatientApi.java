package orci.or.tz.appointments.web.api;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import orci.or.tz.appointments.dto.patient.PatientDto;
import orci.or.tz.appointments.exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@RequestMapping("api/patient")
@Api(value = "Patient Management", description = "Manage Patient on the web")
public interface PatientApi {

    @ApiOperation(value = "View All Patient By Registration Number", notes = "View All Patient By Registration Number")
    @RequestMapping(value = "/regno", method = RequestMethod.GET, produces = "application/json")
    ResponseEntity<PatientDto> GetPatientByRegistration(@RequestParam String regNo) throws ResourceNotFoundException, IOException;
}
