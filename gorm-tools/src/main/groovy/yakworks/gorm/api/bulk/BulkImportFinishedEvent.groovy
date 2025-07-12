/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.bulk

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import gorm.tools.job.SyncJobContext

/**
 * event fired right after finishJob() for bulk imports
 * @param <D> the generic this is for
 */
@CompileStatic
class BulkImportFinishedEvent<D> extends ApplicationEvent implements ResolvableTypeProvider   {

    Long jobId
    Boolean ok
    SyncJobContext context
    Class<D> entityClass

    BulkImportFinishedEvent(SyncJobContext ctx, Class<D> entityClass) {
        super(ctx)
        this.context = ctx
        this.jobId = ctx.jobId
        this.ok = ctx.ok.get()
        assert ctx.args.entityClass
        this.entityClass = entityClass
    }

    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass))
    }
}
