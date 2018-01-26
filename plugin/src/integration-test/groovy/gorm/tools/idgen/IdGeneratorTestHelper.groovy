package gorm.tools.idgen

import groovy.transform.CompileStatic
import org.springframework.jdbc.core.JdbcTemplate

@CompileStatic
class IdGeneratorTestHelper {

    static void createTables(JdbcTemplate jdbcTemplate) {
        //createNewObjectIdTable(jdbcTemplate)
        createCustomTable(jdbcTemplate)
    }

    static void createNewObjectIdTable(JdbcTemplate template) {
        String query = """
        create table NewObjectId
            (
                KeyName varchar(255) not null,
                NextId bigint not null
            )
         """

        template.execute(query)
    }

    static void createCustomTable(JdbcTemplate template) {
        String query = """
            create table if not exists Custom1
                (
                    id int not null,
                    version int null
                )
             """

        template.execute(query)
    }

}
