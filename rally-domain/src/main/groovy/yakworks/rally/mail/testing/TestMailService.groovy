/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail.testing

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import yakworks.api.Result
import yakworks.api.problem.data.DataProblem
import yakworks.rally.mail.EmailService

/**
 * Used for testing to swap out MailService and check Map for results
 */
@Slf4j
@CompileStatic
class TestMailService extends EmailService {

    List sentMail = []

    /**
     * to fail put "bad" in for domain or "bad@email.com" in for the mailTo.to
     */
    @Override
    Result send(String domain, MailTo mailTo){
        if(domain == "bad" || mailTo.to[0] == "bad@email.com"){
            return new DataProblem().title("Send Failure").detail("bad mail").status(401)
        }
        sentMail.add([domain: domain, mailMsg: mailTo])
        //simulate payload with id and message like we get from MailGun
        Map payload = [id: "<${UUID.randomUUID().toString()}@example.com>", message: "Queued. Thank you."]
        return Result.OK().payload(payload)
    }

}
