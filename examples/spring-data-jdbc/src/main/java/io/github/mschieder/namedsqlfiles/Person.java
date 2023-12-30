package io.github.mschieder.namedsqlfiles;

import org.springframework.data.annotation.Id;

public class Person {
  @Id

  private Long id;
  private String firstname;
  private String lastname;

  public void setId(Long id) {
    this.id = id;
  }

  public Long getId() {
    return id;
  }

  public String getFirstname() {
    return firstname;
  }

  public void setFirstname(String firstname) {
    this.firstname = firstname;
  }

  public String getLastname() {
    return lastname;
  }

  public void setLastname(String lastname) {
    this.lastname = lastname;
  }
}
