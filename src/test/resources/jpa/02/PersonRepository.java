package io.github.mschieder.namedsqlfiles;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query(name = "sql/person/getByLastname.sql", nativeQuery = true)
    Optional<Person> findByLastname(String name);

    @Query(countName = "sql/person/count.sql", name = "sql/person/all.sql", nativeQuery = true)
    Page<Person> all(Pageable pageable);
}
