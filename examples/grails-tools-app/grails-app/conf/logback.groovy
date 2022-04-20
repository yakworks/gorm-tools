import grails.util.BuildSettings
import grails.util.Environment

String defaultPattern = '%.-2level %relative %logger{1} - %message%n'
//String defaultPattern = "%level %logger - %msg%n"
// See http://logback.qos.ch/manual/groovy.html for details on configuration

File targetDir = BuildSettings.TARGET_DIR

appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern
    }
}

appender 'file', FileAppender, {
    file = "$targetDir/file.log"
    append = false
    encoder(PatternLayoutEncoder) {
        pattern = defaultPattern
    }
}

root ERROR, ['file']

logger 'grails.app.controllers', DEBUG, ['STDOUT']
logger 'grails.app.services', INFO, ['STDOUT']

logger "yakworks.grails", DEBUG
logger "grails.app.services.foobar", DEBUG
logger "foobar", DEBUG
logger "gorg.grails.gsp.GroovyPageResourceLoader", DEBUG
logger "gorg.grails.gsp.io.DefaultGroovyPageLocator", DEBUG


if (Environment.developmentMode && targetDir) {

    appender('FULL_STACKTRACE', FileAppender) {
        file = "$targetDir/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = defaultPattern
        }
    }

    logger 'StackTrace', ERROR, ['FULL_STACKTRACE'], false

    logger "yakworks.grails", DEBUG,['STDOUT']
    logger "grails.app.services.foobar", DEBUG,['STDOUT']
    logger "org.grails.gsp.GroovyPageResourceLoader", DEBUG,['STDOUT']
    logger "org.grails.gsp.io.DefaultGroovyPageLocator", DEBUG,['STDOUT']
}
