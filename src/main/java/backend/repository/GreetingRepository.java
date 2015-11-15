package backend.repository;

import backend.model.Greeting;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Philipp on 15.11.2015.
 */
public interface GreetingRepository extends CrudRepository<Greeting, Long> {
    List<Greeting> findAll();

    Greeting findOne(Long id);

    void delete(Greeting greeting);
}
