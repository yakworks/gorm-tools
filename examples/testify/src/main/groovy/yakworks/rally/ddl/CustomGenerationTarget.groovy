package yakworks.rally.ddl

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
        if (command =~ /foreign key/){
            println "FILTERING OUT: $command"
            return false
        }
        return true
    }
}
