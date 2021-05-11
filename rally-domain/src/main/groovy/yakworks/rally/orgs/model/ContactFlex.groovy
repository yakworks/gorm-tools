/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import java.time.LocalDateTime

import gorm.tools.audit.AuditStamp
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class ContactFlex implements RepoEntity<ContactFlex>, Serializable{

    static belongsTo = [contact: Contact]

    String text1
    String text2
    String text3
    String text4
    String text5
    String text6
    String text7
    String text8
    String text9
    String text10
    LocalDateTime date1
    LocalDateTime date2
    LocalDateTime date3
    LocalDateTime date4
    BigDecimal num1
    BigDecimal num2
    BigDecimal num3
    BigDecimal num4
    BigDecimal num5
    BigDecimal num6

    static mapping = {
        id generator: 'foreign', params: [property: 'contact']
    }

}
