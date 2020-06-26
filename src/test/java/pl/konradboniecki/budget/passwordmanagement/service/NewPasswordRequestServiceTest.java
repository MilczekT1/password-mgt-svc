package pl.konradboniecki.budget.passwordmanagement.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;

import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
        webEnvironment = NONE,
        properties = "spring.cloud.config.enabled=false"
)
public class NewPasswordRequestServiceTest {

    @MockBean
    private NewPasswordRequestRepository newPasswordRequestRepository;
    @Autowired
    private NewPasswordRequestService newPasswordRequestService;

    @Test
    public void givenNewEntity_whenSaveEntity_thenSavedWithRepository() {
        // Given:
        NewPasswordRequest newPasswordRequest = new NewPasswordRequest();
        when(newPasswordRequestRepository.save(newPasswordRequest))
                .thenReturn(newPasswordRequest);
        // When:
        newPasswordRequestService.saveNewPasswordRequest(newPasswordRequest);
        // Then:
        verify(newPasswordRequestRepository, times(1)).save(newPasswordRequest);
    }
}
