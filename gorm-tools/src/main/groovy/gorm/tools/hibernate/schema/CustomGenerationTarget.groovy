/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.schema

import groovy.transform.CompileStatic

import org.hibernate.resource.transaction.spi.DdlTransactionIsolator
import org.hibernate.tool.schema.internal.exec.GenerationTargetToDatabase

@CompileStatic
class CustomGenerationTarget extends GenerationTargetToDatabase {

    CustomGenerationTarget(DdlTransactionIsolator ddlTransactionIsolator, boolean releaseAfterUse) {
        super(ddlTransactionIsolator, releaseAfterUse)
    }

    @Override
    void accept(String command) {
        if (shouldAccept(command))
            super.accept(command)
    }

    boolean shouldAccept(String command) {
        // Custom filtering logic here, e.g.:
        //if (command =~ /references legacy\.xyz/)
        //don't generate all foreign keys
        // println command
        return !(command =~ /foreign key/)
    }
}
