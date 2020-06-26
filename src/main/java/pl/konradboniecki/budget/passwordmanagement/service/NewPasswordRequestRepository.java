package pl.konradboniecki.budget.passwordmanagement.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.konradboniecki.budget.passwordmanagement.model.NewPasswordRequest;

import java.util.Optional;

@Repository
public interface NewPasswordRequestRepository extends CrudRepository<NewPasswordRequest, Long> {

    Optional<NewPasswordRequest> findById(Long aLong);

    NewPasswordRequest save(NewPasswordRequest entity);

    long count();

    void deleteById(Long aLong);

    boolean existsById(Long id);
}
