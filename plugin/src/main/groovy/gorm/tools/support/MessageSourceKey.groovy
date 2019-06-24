package gorm.tools.support

import groovy.transform.CompileStatic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import org.springframework.context.MessageSourceResolvable

/**
 * Gorm Tools default implementation of the MsgSourceResolvableTrait and thus the {@link MessageSourceResolvable} interface.
 * similiar to {@link org.springframework.context.support.DefaultMessageSourceResolvable}
 * @see org.springframework.context.MessageSource#getMessage(MessageSourceResolvable, java.util.Locale)
 */
@SuppressWarnings("serial")
@CompileStatic
@EqualsAndHashCode
@ToString(includes = ['msgCodes', 'msgArgs', 'defaultMessage'], includeNames = true)
class MessageSourceKey implements MsgSourceResolvableTrait, Serializable {

    /**
     * Create a new empty MessageSourceKey.
     */
    MessageSourceKey() {}

    /**
     * Create a new MessageSourceKey.
     * @param code the code to be used to resolve this message
     */
    MessageSourceKey(String code) {
        this([code], null, null)
    }

    /**
     * Create a new MessageSourceKey.
     * @param codes the codes to be used to resolve this message
     */
    MessageSourceKey(List<String> codes) {
        this(codes, null, null)
    }

    MessageSourceKey(String code, String defaultMessage) {
        this([code], null, defaultMessage)
    }

    MessageSourceKey(String code, List arguments, String defaultMessage) {
        this([code], arguments, defaultMessage)
    }

    MessageSourceKey(Map msgMap) {
        this([msgMap.code as String], msgMap.args as List, msgMap.defaultMessage as String)
    }
    /**
     * Copy constructor: Create a new instance from another resolvable.
     * @param resolvable the resolvable to copy from
     */
    MessageSourceKey(MessageSourceResolvable resolvable) {
        this(resolvable.codes.toList(), resolvable.arguments.toList(), resolvable.defaultMessage)
    }

    /**
     * Create a new MessageSourceKey.
     * @param codes the codes to be used to resolve this message
     * @param arguments the array of arguments to be used to resolve this message
     * @param defaultMessage the default message to be used to resolve this message
     */
    MessageSourceKey(List<String> codes, List arguments, String defaultMessage) {
        this.msgCodes = codes
        this.args = arguments
        this.defaultMessage = defaultMessage
    }

}

