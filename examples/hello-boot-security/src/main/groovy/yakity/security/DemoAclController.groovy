package yakity.security

import groovy.transform.CompileStatic

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@CompileStatic
@RestController
@RequestMapping("/api/acl/test")
class DemoAclController {

    @GetMapping("/")
    def index() {
        "get-check"
    }

    @PostMapping("/")
    def save() {
        "post-check"
    }

    @PutMapping("/")
    def update() {
        "put-check"
    }

    @DeleteMapping("/")
    def delete() {
        "delete-check"
    }
}
