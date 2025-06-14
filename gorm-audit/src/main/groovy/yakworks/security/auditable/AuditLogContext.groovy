/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.auditable


import org.springframework.core.NamedThreadLocal

/**
 * Allows runtime overriding of any audit logging configurable property
 */
class AuditLogContext {
    static final ThreadLocal<Map> auditLogConfig = new NamedThreadLocal<Map>("auditLog.context")

    /**
     * Overrides most 'grails.plugin.auditLog' configuration properties for execution of the given block
     *
     * For example:
     *
     * withConfig(logIncludes: ['foo', 'bar']) { ... }
     * withConfig(logFullClassName: false) { ... }
     * withConfig(verbose: false) { ... }
     *
     */
    static withConfig(Map config, Closure block) {
        def previousValue = null
        try {
            // First getting and then setting should be okay as we use a ThreadLocal so no race conditions should be possible
            previousValue = auditLogConfig.get()
            auditLogConfig.set(mergeConfig(config))
            block.call()
        }
        finally {
            auditLogConfig.set(previousValue)
        }
    }

    /**
     * Disable verbose audit logging for anything within this block
     */
    static withoutVerboseAuditLog(Closure block) {
        withConfig([verbose: false], block)
    }

    /**
     * Disable audit logging for this block
     */
    static withoutAuditLog(Closure block) {
        withConfig([disabled: true], block)
    }

    /**
     * @return the current context
     */
    static Map getContext() {
        mergeConfig(auditLogConfig.get())
    }

    private static Map mergeConfig(Map localConfig) {
        AuditLoggingConfigUtils.auditConfig + (localConfig ?: [:])
    }
}
