/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import java.time.LocalDate

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.RepoEntity
import gorm.tools.security.domain.AppUser
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.common.NameNum
import yakworks.rally.orgs.repo.ContactRepo

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class Contact implements NameNum, RepoEntity<Contact>, Serializable {
    String num
    String name
    String entityName // used for jobs contacts at ced

    // belongs to Org, we don't use static belongsTo because Org also has contactId
    Org org

    Long visibleToOrgType //which orgs type is this visible to (prospects and customers)

    Boolean tagForReminders = false
    Boolean inactive = false
    Boolean isPrimary = false
    //ContactType type  // billing, shipping, etc

    String email //default
    String phone //default

    String firstName
    String middleName
    String lastName
    String nickName
    String salutation
    String jobTitle
    String department
    LocalDate birthday
    String comments

    Boolean isLocationDifferent = false
    Location location
    ContactFlex flex //user fields
    AppUser user

    static hasMany = [phones: ContactPhone, emails: ContactEmail, sources: ContactSource]

    static mapping = {
        cache true
        org column: 'orgId'
        flex column: 'flexId'
        location column: 'locationId'
        user column: 'userId'

        flex cascade: "all"
        emails cascade: "all-delete-orphan"
        phones cascade: "all-delete-orphan"
        sources cascade: "all-delete-orphan"
    }

    static constraints = {
        num nullable: true, maxSize: 50
        name nullable: false, blank: false, maxSize: 50

        entityName nullable: true
        inactive nullable: false
        isPrimary nullable: false

        isLocationDifferent nullable: false
        location nullable: true
        phone nullable: true
        email email: true, nullable: true

        firstName blank: false, nullable: false, maxSize: 50
        middleName nullable: true, maxSize: 50
        lastName nullable: true, maxSize: 50
        nickName nullable: true, maxSize: 50
        salutation nullable: true, maxSize: 50
        jobTitle nullable: true, maxSize: 50
        department nullable: true, maxSize: 50
        birthday nullable: true
        comments nullable: true

        tagForReminders nullable: false
        org nullable: false
        visibleToOrgType nullable: true

        flex nullable: true
        user nullable: true
    }

    static ContactRepo getRepo() { RepoUtil.findRepo(this) as ContactRepo }

    List<Location> getLocations(){
        Location.listByContact(this)
    }

    boolean isUserEnabled() {
        return (user && user.enabled)
    }

    void concatName() {
        String fullName = ((firstName ?: "") + ' ' + (lastName ?: "")).trim()
        name = fullName.size() > 50 ? fullName[0..49] : fullName
    }

//  static void contactName(Contact contact) {
//  def fullName =(contact.firstName ?: "") + ' ' + (contact.lastName ?: "")
//  contact.name = fullName.size() > 50 ? fullName[0..49] : fullName
//  }

    /**
     * If contact has a locationId and isLocationDifferent=false, otherwise use the main location from the org.
     */
    Location getAddress() {
        return location ?: org?.location
    }

    static List<Contact> listActive(Long orgId) {
        return Contact.query(ordId: orgId, inactive: false).list()
    }

    static Contact findByUser(AppUser user){
        Contact.findWhere([user: user], [cache: true])
    }
}

//enum ContactType {
//  Billing("Billing"),Shipping("Shipping"); // TODO add more
//
//  final String contactType
//
//  ContactType(String contactType) {
//      this.contactType = contactType;
//  }
//  static List stringValues(){
//  return SourceType.values().toList().collect{it.contactType}
//  }
//}
