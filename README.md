# spring-data-jpa-namedsqlfiles

An annotation processor, that processes references to SQL resource files in Spring Data's Query
annotation parameters "name" and "countName" and converts their file content to standard named properties file
'META-INF/jpa-named-queries.properties'.
So some (complex) SQL statements can stay separated from the repository source code and do not lose their custom SQL
code style.

## Usage

1. Add the Maven dependency:
   ```xml
   <dependency>
       <groupId>com.github.mschieder</groupId>
       <artifactId>spring-data-jpa-namedsqlfiles</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ```

2. Add a complex SQL statement in its own SQL file
   src/main/resources/sql/person/complex.sql

   ```sql
   SELECT FIRSTNAME, LASTNAME, BIRTHDATE
   FROM PERSON
   WHERE OID = ?
   ```

3. Add a query method to your spring-data-jpa repository, which references the created SQL resource file with "name"
   or "countName"

   ```java
   public interface PersonRepository extends JpaRepository<Person, Long> {
       @Query(name = "sql/person/complex.sql")
       PersonDto findById(Long oid);
   }
   ```

4. The annotation processor generates the 'META-INF/jpa-named-queries.properties' in your class output directory at
   compile time:
   ```properties
   sql/person/complex.sql=SELECT FIRSTNAME, LASTNAME, BIRTHDATE\r\nFROM PERSON\r\nWHERE OID = ?
   ```

5. Spring Data JPA can access the named query "sql/person/complex.sql" at runtime out-of-the-box.

## Notes

* the SQL resource files must be stored in the same project as the using repository
* if a 'META-INF/jpa-named-queries.properties' exists the referenced SQL statements are merged into it
* only files with suffix .sql are reported as compile error, if they are missing

## License

spring-data-jpa-namedsqlfiles is released under version 2.0 of
the [Apache License](https://www.apache.org/licenses/LICENSE-2.0).

