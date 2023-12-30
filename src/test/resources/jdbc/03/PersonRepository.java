package io.github.mschieder.namedsqlfiles;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface PersonRepository extends CrudRepository<Person, Long> {
  Optional<Person> findByLastname(String lastname);
  List<Person> findAll();
}
