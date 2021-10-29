Taken from this stackover flow so we can filter out ddl. 
We do this to filter out the foreign keys which we consider a big nuisance and performance hit
when dealing with gorm. We normally hand select the ones that dont hurt batch inserts or deletes using liquibase.
but for testing we don't want any.

This is set in application.yml with
hibernate:
schema_management_tool: 'gorm.tools.hibernate.schema.CustomSchemaManagementTool'
