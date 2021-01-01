package yakworks.taskify.domain

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic

@AuditStamp
@GrailsCompileStatic
class Contact implements RepoEntity<Contact>{ // implements AuditStampTrait {
    String firstName
    String lastName
    String email

    LocalDate dateOfBirth
    TimeZone timeZone
    LocalDateTime activateOnDate

    Integer age

    Date dateCreated
    Date lastUpdated
    Boolean inactive
    Salutations salutation

    static hasOne = [address: ContactAddress]

    static constraints = {
        firstName nullable: false
        dateOfBirth nullable: true
        inactive bindable: false, display: false, editable: false
    }

}

@CompileDynamic
enum Salutations {
    Ninja, Mr, Mrs, Ms, Dr, Rev
}
