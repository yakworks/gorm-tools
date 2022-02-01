/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import groovy.transform.CompileStatic

@CompileStatic
trait BaseArScoreCard {

    BigDecimal aging1
    BigDecimal aging2
    BigDecimal aging3
    BigDecimal aging4
    BigDecimal aging5
    BigDecimal aging6
    BigDecimal aging7
    BigDecimal aging8
    BigDecimal aging9
    BigDecimal aging10

    //Performance metrics /KPI's
    BigDecimal avgDaysBt //avg days paid late / avg days beyond terms
    BigDecimal adp // avg days to pay
    BigDecimal adpWtd // weighted average days to pay
    BigDecimal cei //collection effectiveness index
    BigDecimal ddo //days deductions and disputes outstanding
    BigDecimal dso //day sales outstanding
    BigDecimal dsoad // DSO add back
    BigDecimal dsobp //DSO best possible
    BigDecimal dso12 //DSO using the last 12 month average

    String glPostPeriod

    //balance due fields
    BigDecimal curBal // whats not due yet
    BigDecimal maxDue // max totalDue in this month and last 12 months
    String maxDuePer // period the highest balance occured in the last 12 months
    BigDecimal pastDue //balance past due
    BigDecimal totalDue // total due, sum of all open items. will be the ending balance when month is closed

    //sales trending
    BigDecimal grossProfit// gross profit
    BigDecimal grossProfitPct// gross profit percent
    BigDecimal salesRolling12
    // rolling 12 months of sales, TODO does this include current period?, is this just invoices?
    BigDecimal salesYtd //YTD sales

    //open trans docType
    BigDecimal openCM
    BigDecimal openDD
    BigDecimal openDM
    BigDecimal openIN
    BigDecimal openPA
    //openTotal is the same as totalDue

    //Roll forward measures
    BigDecimal beginBal //totalDue from prior period
    //created trans
    BigDecimal newCM
    BigDecimal newDD
    BigDecimal newDM
    BigDecimal newIN
    BigDecimal newPA
    BigDecimal newTotal //shortcut calc for the sum of the above

    //adjustments. used for roll forward
    BigDecimal adjusterAmt //the sum of origAmount of the ArAdjust.arTran
    BigDecimal adjustedTotal //the sum of arAdjustLine.amount
    /* maybe for future?
    BigDecimal adjustedCM
    BigDecimal adjustedDD
    BigDecimal adjustedDM
    BigDecimal adjustedIN
    BigDecimal adjustedPA
    */

    //flex fields for other open stats
    BigDecimal num1
    BigDecimal num2
    BigDecimal num3
    BigDecimal num4
    BigDecimal num5
    BigDecimal num6

}
