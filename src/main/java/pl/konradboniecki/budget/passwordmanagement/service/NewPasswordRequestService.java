package pl.konradboniecki.budget.passwordmanagement.service;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;

import java.util.Optional;

@Slf4j
@Service
public class NewPasswordRequestService {

    @Setter(AccessLevel.PRIVATE)
    private NewPasswordRequestRepository newPasswordRequestRepository;

    public NewPasswordRequestService(NewPasswordRequestRepository newPasswordRequestRepository) {
        setNewPasswordRequestRepository(newPasswordRequestRepository);
    }

    public void saveNewPasswordRequest(NewPasswordRequest newPasswordRequest) {
        newPasswordRequestRepository.save(newPasswordRequest);
    }

    public void deleteNewPasswordRequestById(Long id) {
        newPasswordRequestRepository.deleteById(id);
    }

    public Optional<NewPasswordRequest> findNewPasswordRequestById(Long id) {
        return newPasswordRequestRepository.findById(id);
    }
}
