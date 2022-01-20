import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.StandardCharsets

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = StandardCharsets.UTF_8

        pattern =
            '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                '%clr(%5p) ' + // Log level
                '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                '%m%n%wex' // Message
    }
}

root(WARN, ['STDOUT'])


logger("org.hibernate", OFF)  //XXX https://github.com/9ci/domain9/issues/600

//TURN ON for benchmarks
// logger "org.hibernate", INFO
//
// // for stats and sql logging
// logger 'org.hibernate.stat', DEBUG
// logger 'org.hibernate.SQL', DEBUG

// this one is very noisy but if neede will show the values passed to sql statments
// logger 'org.hibernate.type.descriptor.sql.BasicBinder', TRACE

// logger 'grails.plugin.springsecurity.rest', TRACE, ['STDOUT']

// this logs out the chunk status
// logger 'gorm.tools.job.SyncJobContext', DEBUG
