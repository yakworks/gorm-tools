/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model


import gorm.tools.model.NameNum
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.repo.ContactRepo
import yakworks.rally.tag.model.Taggable
import yakworks.security.audit.AuditStamp
import yakworks.security.gorm.model.AppUser

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class Contact implements NameNum, RepoEntity<Contact>, Taggable, Serializable {
    // some Contacts get a num, such as for job contacts
    String num
    //the joined name from firstName lastName
    String name
    // alternate name,nicknames and used for jobs contacts at ced
    String altName

    String firstName
    String lastName

    String email
    String phone

    // belongs to Org, we don't use static belongsTo because Org also has contactId
    // the org is readonly and is here for queries
    Org org
    // the key that needs to be populated
    Long orgId

    Boolean tagForReminders = false //make an actual tag
    Boolean inactive = false

    String comments

    //remove
    //Long visibleToOrgType //which orgs type is this visible to (prospects and customers)
    //ContactType type  // billing, shipping, etc
    //Boolean isPrimary = false
    //Boolean isLocationDifferent = false

    //FIXME move to ContactInfo or remove from db
    // String middleName
    // String nickName
    // String salutation
    // String department
    // LocalDate birthday
    String jobTitle

    Location location
    ContactFlex flex
    AppUser user

    ContactSource source

    static hasMany = [phones: ContactPhone, emails: ContactEmail]

    static List<String> toOneAssociations = ['flex']

    static Map includes = [
        get: ['id', 'num', 'name', 'altName', 'firstName', 'lastName', 'email', 'phone', 'inactive', 'org', 'user'],
        qSearch: ['num', 'name', 'altName', 'email'],
        stamp: ['id', 'name']  //picklist or minimal for joins
    ]

    static mapping = {
        cache "read-write"
        orgId column: 'orgId'
        org column: 'orgId', insertable: false, updateable: false
        flex column: 'flexId'
        location column: 'locationId'
        user column: 'userId'
        //FIXME temp mapping until column change
        altName column: 'entityName'
        source column: 'contactSourceId'

        flex cascade: "all"
        emails cascade: "all-delete-orphan"
        phones cascade: "all-delete-orphan"
    }

    static constraintsMap = [
        num:[ d:'num, used for job or orgnization type contacts', nullable: true],
        name:[ nullable: false],

        altName:[d:'alternate name, nickname or job name', nullable: true, maxSize: 255],
        inactive:[ d:'is active', nullable: false],
        // isPrimary:[ nullable: false],

        // isLocationDifferent:[ nullable: false],
        location:[ nullable: true],
        phone:[ d:'default email', nullable: true, maxSize: 255],
        email:[ d:'default phone', email: true, nullable: true, maxSize: 255],
        //FIXME this is different than how we do OrgSource and ArTranSource, we require it in those cases.
        // if we dont require it then we need to think through the implication in our design.
        source:[ d: 'Originator source info', oapi:[read: true, create: ['source', 'sourceType', 'sourceId']], bindable: false],

        firstName:[ nullable: false, maxSize: 50],
        // middleName:[ nullable: true, maxSize: 50],
        lastName:[ nullable: true, maxSize: 50],
        // nickName:[ nullable: true, maxSize: 50],
        // salutation:[ nullable: true, maxSize: 50],
        jobTitle:[ d:'Job title', nullable: true, maxSize: 50],
        // department:[ nullable: true, maxSize: 50],
        // birthday:[ nullable: true],
        comments:[ d:'notes about the contact', nullable: true, maxSize: 255],

        tagForReminders:[ d:'if this contact should get correspondence', nullable: false],
        org:[ description: 'The organization this contact belongs to'],
        orgId:[ description: 'The org id for the contact', nullable: false],

        // visibleToOrgType:[ nullable: true],

        flex:[ d:'custom user fields for this contact', nullable: true],
        user:[ d:'the user if this contact is able to login', nullable: true],
    ]

    static ContactRepo getRepo() { RepoLookup.findRepo(this) as ContactRepo }

    List<Location> getLocations(){
        Location.listByContact(this)
    }

    boolean isUserEnabled() {
        return (user && user.enabled)
    }

    /** flag 'isPrimary' to be displayed on grid on list of contacts under Org (Customer) */
    boolean getIsPrimary() {
        return (id == org?.contact?.getId())
    }


    /**
     * If contact has a locationId and isLocationDifferent=false, otherwise use the main location from the org.
     */
    Location getAddress() {
        return location ?: org?.location
    }

    void setOrg(Org o){
        org = o
        if(o.id != orgId) orgId = o.getId()
    }

    /**
     * List the active contacts for the org
     */
    static List<Contact> listActive(Long orgId) {
        return Contact.query(orgId: orgId, inactive: false).list()
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
