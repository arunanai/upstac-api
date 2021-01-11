package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.exception.AppException;
import org.upgrad.upstac.testrequests.TestRequest;
import org.upgrad.upstac.testrequests.consultation.ConsultationController;
import org.upgrad.upstac.testrequests.consultation.CreateConsultationRequest;
import org.upgrad.upstac.testrequests.consultation.DoctorSuggestion;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;
import org.upgrad.upstac.testrequests.RequestStatus;
import org.upgrad.upstac.testrequests.TestRequestQueryService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Slf4j
class ConsultationControllerTest {


    @Autowired
    ConsultationController consultationController;

    @Autowired
    LabRequestController labRequestController;


    @Autowired
    TestRequestQueryService testRequestQueryService;


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_update_the_request_status(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_COMPLETED);

        //Act
        TestRequest testRequest1 = consultationController.assignForConsultation(testRequest.requestId);

        //Assert
        assertNotNull(testRequest1.getConsultation());
        assertThat(testRequest.requestId, is(testRequest1.requestId));
        assertThat(testRequest1.getStatus(), is(RequestStatus.DIAGNOSIS_IN_PROCESS));
    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_assignForConsultation_with_valid_test_request_id_should_throw_exception(){

        //Arrange
        Long InvalidRequestId= -34L;

        //Act
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,()->{

            consultationController.assignForConsultation(InvalidRequestId);
        });

        //Assert
        assertThat(responseStatusException.getMessage(),containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_valid_test_request_id_should_update_the_request_status_and_update_consultation_details(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);

        //Act
        TestRequest updatedRequest = consultationController.updateConsultation(testRequest.requestId, createConsultationRequest);


        //Assert
        assertThat(testRequest.requestId, is(updatedRequest.requestId));
        assertThat(updatedRequest.getStatus(), is(RequestStatus.COMPLETED));

    }


    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_test_request_id_should_throw_exception(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);
        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        Long InvalidRequestId= -34L;

        //Act
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,()->{

            consultationController.updateConsultation(InvalidRequestId, createConsultationRequest);
        });

        //Assert
        assertThat(responseStatusException.getMessage(),containsString("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "doctor")
    public void calling_updateConsultation_with_invalid_empty_status_should_throw_exception(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.DIAGNOSIS_IN_PROCESS);

        CreateConsultationRequest createConsultationRequest = getCreateConsultationRequest(testRequest);
        createConsultationRequest.setSuggestion(null);

        //Act, Assert
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,()->{

            consultationController.updateConsultation(testRequest.requestId, createConsultationRequest);
        });


    }

    public CreateConsultationRequest getCreateConsultationRequest(TestRequest testRequest) {

        CreateLabResult labResult = new CreateLabResult();
        labResult.setResult(TestStatus.NEGATIVE);
        labResult.setComments("Negative");
        labResult.setBloodPressure("normal");
        labResult.setHeartBeat("normal");
        labResult.setTemperature("normal");
        labResult.setOxygenLevel("normal");

        CreateConsultationRequest createConsultationRequest = new CreateConsultationRequest();

        if(labResult.getResult() == TestStatus.NEGATIVE)
        {
            createConsultationRequest.setSuggestion(DoctorSuggestion.NO_ISSUES);
            createConsultationRequest.setComments("Ok");
        }
        else
        {
            createConsultationRequest.setSuggestion(DoctorSuggestion.HOME_QUARANTINE);
            createConsultationRequest.setComments("Please home quarantine for 14 days");
        }
        return createConsultationRequest;

    }

}