/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.schema

import groovy.transform.CompileStatic

import org.hibernate.boot.Metadata
import org.hibernate.tool.schema.internal.HibernateSchemaManagementTool
import org.hibernate.tool.schema.internal.SchemaDropperImpl
import org.hibernate.tool.schema.internal.exec.GenerationTarget
import org.hibernate.tool.schema.internal.exec.JdbcContext
import org.hibernate.tool.schema.spi.ExecutionOptions
import org.hibernate.tool.schema.spi.SchemaFilter
import org.hibernate.tool.schema.spi.SourceDescriptor
import org.hibernate.tool.schema.spi.TargetDescriptor

@CompileStatic
class CustomSchemaDropper extends SchemaDropperImpl {
    private final HibernateSchemaManagementTool tool

    CustomSchemaDropper(HibernateSchemaManagementTool tool, SchemaFilter schemaFilter) {
        super(tool, schemaFilter)
        this.tool = tool
    }

    @Override
    void doDrop(Metadata metadata, ExecutionOptions options, SourceDescriptor sourceDescriptor, TargetDescriptor targetDescriptor) {
        final JdbcContext jdbcContext = tool.resolveJdbcContext( options.getConfigurationValues() )
        final GenerationTarget[] targets = new GenerationTarget[ targetDescriptor.getTargetTypes().size() ]
        targets[0] = new CustomGenerationTarget(tool.getDdlTransactionIsolator(jdbcContext), true)
        super.doDrop(metadata, options, jdbcContext.getDialect(), sourceDescriptor, targets)
    }
}
