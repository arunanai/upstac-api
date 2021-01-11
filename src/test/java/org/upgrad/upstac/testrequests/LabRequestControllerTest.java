package org.upgrad.upstac.testrequests;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.web.server.ResponseStatusException;
import org.upgrad.upstac.testrequests.lab.CreateLabResult;
import org.upgrad.upstac.testrequests.lab.LabRequestController;
import org.upgrad.upstac.testrequests.lab.TestStatus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@Slf4j
class LabRequestControllerTest {


    @Autowired
    LabRequestController labRequestController;




    @Autowired
    TestRequestQueryService testRequestQueryService;


    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_update_the_request_status(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.INITIATED);
        //Implement this method

        //Act
        TestRequest newRequest = labRequestController.assignForLabTest(testRequest.requestId);

        //Assert
        assertThat(testRequest.requestId, is(newRequest.requestId));
        assertThat(newRequest.getStatus(), is(RequestStatus.INITIATED));
        assertNotNull(newRequest.getLabResult());


    }

    public TestRequest getTestRequestByStatus(RequestStatus status) {
        return testRequestQueryService.findBy(status).stream().findFirst().get();
    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_assignForLabTest_with_valid_test_request_id_should_throw_exception(){

        //Arrange
        Long InvalidRequestId= -34L;

        //Act
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,()->{

            labRequestController.assignForLabTest(InvalidRequestId);
        });


        //Assert
        assertThat(responseStatusException.getMessage(),comparesEqualTo("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_valid_test_request_id_should_update_the_request_status_and_update_test_request_details(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);

        //Act
        TestRequest newRequest = labRequestController.updateLabTest(testRequest.requestId, labResult);
        newRequest.setStatus(RequestStatus.LAB_TEST_IN_PROGRESS);

        //Assert
        assertThat(testRequest.requestId, is(newRequest.requestId));
        assertThat(newRequest.getStatus(), is(RequestStatus.LAB_TEST_COMPLETED));
        assertThat(testRequest.getLabResult(),is(newRequest.getLabResult()));
    }


    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_test_request_id_should_throw_exception(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        Long InvalidRequestId= -34L;
        CreateLabResult labResult = getCreateLabResult(testRequest);

        //Act
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,()->{

            labRequestController.updateLabTest(InvalidRequestId, labResult);
        });

        //Assert
        assertThat(responseStatusException.getMessage(),comparesEqualTo("Invalid ID"));

    }

    @Test
    @WithUserDetails(value = "tester")
    public void calling_updateLabTest_with_invalid_empty_status_should_throw_exception(){

        //Arrange
        TestRequest testRequest = getTestRequestByStatus(RequestStatus.LAB_TEST_IN_PROGRESS);
        CreateLabResult labResult = getCreateLabResult(testRequest);
        labResult.setResult(null);

        //Act
        ResponseStatusException responseStatusException = assertThrows(ResponseStatusException.class,()->{

            labRequestController.updateLabTest(testRequest.requestId, labResult);
        });

        //Assert
        assertThat(responseStatusException.getMessage(),comparesEqualTo("ConstraintViolationException"));

    }

    public CreateLabResult getCreateLabResult(TestRequest testRequest) {
        CreateLabResult labResult = new CreateLabResult();
        labResult.setResult(TestStatus.NEGATIVE);
        labResult.setComments("Negative");
        labResult.setBloodPressure("normal");
        labResult.setHeartBeat("normal");
        labResult.setTemperature("normal");
        labResult.setOxygenLevel("normal");

        return labResult;
    }

}