package io.github.mschieder.namedsqlfiles;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS,
    statements =
        "create table person (id bigint not null auto_increment, firstname varchar(255),"
            + " lastname varchar(255), primary key (id))")
class PersonRepositoryTest {

  @Autowired private PersonRepository personRepository;

  @BeforeEach
  void initDb() {
    given("John", "Doe");
    given("Jane", "Doe");
    given("Max", "Doe");
    given("Max", "Mustermann");
  }

  @AfterEach
  void tearDown() {
    personRepository.deleteAll();
  }

  @Test
  void testNativeQuery() {
    assertThat(personRepository.findByLastname("Mustermann")).isNotEmpty();
  }

  private void given(String firstname, String lastname) {
    Person person = new Person();
    person.setFirstname(firstname);
    person.setLastname(lastname);
    personRepository.save(person);
  }

  @Test
  void testQueryAll() {
    assertThat(personRepository.all()).hasSize(4);
  }
}
