package backend.model.user;

import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<UserCore, Long> {
    User findByEmail(String email);
}
