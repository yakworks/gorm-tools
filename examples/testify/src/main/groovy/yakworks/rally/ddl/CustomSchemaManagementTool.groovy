package yakworks.rally.ddl

import groovy.transform.CompileStatic

import org.hibernate.boot.registry.selector.spi.StrategySelector
import org.hibernate.cfg.AvailableSettings
import org.hibernate.tool.schema.internal.DefaultSchemaFilterProvider
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool
import org.hibernate.tool.schema.spi.SchemaCreator
import org.hibernate.tool.schema.spi.SchemaDropper
import org.hibernate.tool.schema.spi.SchemaFilterProvider

/**
 * Taken from this stackover flow so we can filter out ddl.
 * We do this to filter out the foreign keys which we consider a big nuisance and performance hit
 * when dealing with gorm. We normally hand select the ones that dont hurt batch inserts or deletes using liquibase.
 * but for testing we don't want any.
 *
 * This is set in application.yml with
 * hibernate:
 *   schema_management_tool: 'yakworks.rally.ddl.CustomSchemaManagementTool'
 *
 */
@CompileStatic
class CustomSchemaManagementTool extends HibernateSchemaManagementTool {
    @Override
    SchemaCreator getSchemaCreator(Map options) {
        return new CustomSchemaCreator(this, getSchemaFilterProvider(options).getCreateFilter())
    }

    @Override
    SchemaDropper getSchemaDropper(Map options) {
        return new CustomSchemaDropper(this, getSchemaFilterProvider(options).getDropFilter())
    }

    // We unfortunately copy this private method from HibernateSchemaManagementTool
    private SchemaFilterProvider getSchemaFilterProvider(Map options) {
        final Object configuredOption = (options == null) ? null : options.get(AvailableSettings.HBM2DDL_FILTER_PROVIDER)
        return serviceRegistry.getService(StrategySelector.class).resolveDefaultableStrategy(
            SchemaFilterProvider.class,
            configuredOption,
            DefaultSchemaFilterProvider.INSTANCE
        )
    }
}
