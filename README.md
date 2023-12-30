# named-sqlfiles-spring-data

Provides annotation processors, that process references to SQL resource files in Spring Data JDBC/JPA's Query
annotation parameters "name" (Spring Data JPA: + "countName") and converts their file content to standard named
properties file 'META-INF/jdbc-named-queries.properties' (Spring Data JDBC) or 'META-INF/jpa-named-queries.properties' (
Spring Data JPA).

So some (complex) SQL statements can stay separated from the repository source code and do not lose their custom SQL
code style.

## Usage

1. Add the Maven dependency to your spring-data-jdbc/spring-data-jpa project:
   ```xml
   <dependency>
       <groupId>io.github.mschieder</groupId>
       <artifactId>named-sqlfiles-spring-data</artifactId>
       <version>1.0.0-SNAPSHOT</version>
       <scope>provided</scope>
   </dependency>
   ```

2. Add a complex SQL statement in its own SQL file
   src/main/resources/sql/person/complex.sql

   ```sql
   SELECT FIRSTNAME, LASTNAME, BIRTHDATE
   FROM PERSON
   WHERE OID = ?
   ```

3. Add a query method to your spring-data-jdbc/spring-data-jpa repository, which references the created SQL resource
   file with "name"
   Spring Data JDBC:
   ```java
   public interface PersonRepository extends CrudRepository<Person, Long> {
       @Query(name = "sql/person/complex.sql")
       PersonDto findById(Long oid);
   }
   ```
   Spring Data JPA:
   ```java
   public interface PersonRepository extends JpaRepository<Person, Long> {
       @Query(name = "sql/person/complex.sql")
       PersonDto findById(Long oid);
   }
   ```

4. The annotation processor generates the 'META-INF/jdbc-named-queries.properties' (Spring Data JDBC)
   / 'META-INF/jpa-named-queries.properties' (Spring Data JPA) in your class output directory at compile time:
   ```properties
   sql/person/complex.sql=SELECT FIRSTNAME, LASTNAME, BIRTHDATE\r\nFROM PERSON\r\nWHERE OID = ?
   ```

5. Spring Data JDBC/JPA can access the named query "sql/person/complex.sql" at runtime out-of-the-box.

## Notes

* the SQL resource files must be stored in the same project as the using repository
* if a 'META-INF/jdbc-named-queries.properties'/'META-INF/jpa-named-queries.properties' exists the referenced SQL
  statements are merged into it
* only files with suffix .sql are reported as compile error, if they are missing

## Building

```bash
mvn clean install
```

## License

named-sqlfiles-spring-data is released under version 2.0 of
the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).

