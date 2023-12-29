package com.github.mschieder.spring.example;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PersonRepository extends JpaRepository<Person, Long> {

    @Query(name = "Person.findByLastname", nativeQuery = true)
    Optional<Person> findByLastname(String name);

    @Query(value = "select from Person")
    Page<Person> all(Pageable pageable);
}
