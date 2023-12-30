package io.github.mschieder.namedsqlfiles;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PersonRepositoryTest {

    @Autowired
    private PersonRepository personRepository;

    @BeforeEach
    void initDb(){
        given(1L, "John", "Doe");
        given(2L, "Jane", "Doe");
        given(3L, "Max", "Doe");
        given(4L, "Max", "Mustermann");
    }

    @Test
    void testNativeQuery(){
        assertThat(personRepository.findByLastname("Mustermann")).isNotEmpty();
    }

    private void given(Long id, String firstname, String lastname){
        Person person = new Person();
        person.setId(id);
        person.setFirstname(firstname);
        person.setLastname(lastname);
        personRepository.saveAndFlush(person);
    }

    @Test
    void testNativeQueryWithCount(){
        assertThat(personRepository.all(Pageable.ofSize(2)).getTotalElements()).isEqualTo(4);
    }
}