package yakworks.rest.log

import org.springframework.context.ApplicationEvent

class InvalidApiCallEvent extends ApplicationEvent {

    String code
    String title
    String detail
    String user
    Object payload

    InvalidApiCallEvent(String code, String title, String detail, String user = null, Object payload = null) {
        super(code)
        this.code = code
        this.title = title
        this.detail = detail
        this.user = user
        this.payload = payload
    }
}
