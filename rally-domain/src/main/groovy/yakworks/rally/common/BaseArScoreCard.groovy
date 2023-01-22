/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileStatic

@CompileStatic
trait BaseArScoreCard {

    BigDecimal aging1 = 0.0
    BigDecimal aging2 = 0.0
    BigDecimal aging3 = 0.0
    BigDecimal aging4 = 0.0
    BigDecimal aging5 = 0.0
    BigDecimal aging6 = 0.0
    BigDecimal aging7 = 0.0
    BigDecimal aging8 = 0.0
    BigDecimal aging9 = 0.0
    BigDecimal aging10 = 0.0

    //Performance metrics /KPI's
    BigDecimal avgDaysBt = 0.0 //avg days paid late / avg days beyond terms
    BigDecimal adp = 0.0 // avg days to pay
    BigDecimal adpWtd = 0.0 // weighted average days to pay
    BigDecimal cei = 0.0 //collection effectiveness index
    BigDecimal ddo = 0.0 //days deductions and disputes outstanding
    BigDecimal dso = 0.0 //day sales outstanding
    BigDecimal dsoad = 0.0 // DSO add back
    BigDecimal dsobp = 0.0 //DSO best possible
    BigDecimal dso12 = 0.0 //DSO using the last 12 month average

    String glPostPeriod

    //balance due fields
    BigDecimal curBal = 0.0 // whats not due yet
    BigDecimal maxDue = 0.0 // max totalDue in this month and last 12 months
    String maxDuePer = 0.0 // period the highest balance occured in the last 12 months
    BigDecimal pastDue = 0.0 //balance past due
    BigDecimal totalDue = 0.0 // total due, sum of all open items. will be the ending balance when month is closed

    //sales trending
    BigDecimal grossProfit = 0.0// gross profit
    BigDecimal grossProfitPct = 0.0// gross profit percent
    BigDecimal salesRolling12 = 0.0
    // rolling 12 months of sales, TODO does this include current period?, is this just invoices?
    BigDecimal salesYtd = 0.0 //YTD sales

    //open trans docType
    BigDecimal openCM = 0.0
    BigDecimal openDD = 0.0
    BigDecimal openDM = 0.0
    BigDecimal openIN = 0.0
    BigDecimal openPA = 0.0
    //openTotal is the same as totalDue

    //Roll forward measures
    BigDecimal beginBal = 0.0 //totalDue from prior period
    //created trans
    BigDecimal newCM = 0.0
    BigDecimal newDD = 0.0
    BigDecimal newDM = 0.0
    BigDecimal newIN = 0.0
    BigDecimal newPA = 0.0
    BigDecimal newTotal = 0.0 //shortcut calc for the sum of the above

    //adjustments. used for roll forward
    BigDecimal adjusterAmt = 0.0 //the sum of origAmount of the ArAdjust.arTran
    BigDecimal adjustedTotal = 0.0 //the sum of arAdjustLine.amount
    /* maybe for future?
    BigDecimal adjustedCM
    BigDecimal adjustedDD
    BigDecimal adjustedDM
    BigDecimal adjustedIN
    BigDecimal adjustedPA
    */

    //flex fields for other open stats
    BigDecimal num1 = 0.0
    BigDecimal num2 = 0.0
    BigDecimal num3 = 0.0
    BigDecimal num4 = 0.0
    BigDecimal num5 = 0.0
    BigDecimal num6 = 0.0

    static constraintsMap = [
        aging1: [d: 'Aging bucket 1', format: 'money'],
        aging2: [d: 'Aging bucket 2', format: 'money'],
        aging3: [d: 'Aging bucket 3', format: 'money'],
        aging4: [d: 'Aging bucket 4', format: 'money'],
        aging5: [d: 'Aging bucket 5', format: 'money'],
        aging6: [d: 'Aging bucket 6', format: 'money'],
        aging7: [d: 'Aging bucket 7', format: 'money'],
        aging8: [d: 'Aging bucket 8', format: 'money'],
        aging9: [d: 'Aging bucket 9', format: 'money'],
        aging10: [d: 'Aging bucket 10', format: 'money'],

        curBal: [d: 'whats not due yet', format: 'money'],
        maxDue: [d: 'max totalDue in this month and last 12 months', format: 'money'],
        maxDuePer: [d: 'period the highest balance occured in the last 12 months'],
        pastDue: [d: 'balance past due', format: 'money'],
        totalDue: [d: 'total due, sum of all open items. will be the ending balance when month is closed', format: 'money'],
        //stored as decimal but by default display with single decimal by default, no need for a dso of 34.12 for example
        avgDaysBt: [d: 'avg days paid late / avg days beyond terms', format: 'decimal(9,1)'],
        adp: [d: 'avg days to pay', format: 'decimal(9,1)'],
        adpWtd: [d: 'weighted average days to pay', format: 'decimal(9,1)'],
        cei: [d: 'collection effectiveness index', format: 'decimal(9,1)'],
        ddo: [d: 'days deductions and disputes outstanding', format: 'decimal(9,1)'],

        dso: [d: 'day sales outstanding', format: 'decimal(9,1)'],
        dsoad: [d: 'DSO add back', format: 'decimal(9,1)'],
        dsobp: [d: 'DSO best possible', format: 'decimal(9,1)'],
        dso12: [d: 'DSO using the last 12 month average', format: 'decimal(9,1)'],
    ]

}
