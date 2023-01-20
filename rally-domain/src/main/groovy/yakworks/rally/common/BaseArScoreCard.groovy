/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileStatic

@CompileStatic
trait BaseArScoreCard {

    BigDecimal aging1 = BigDecimal.ZERO
    BigDecimal aging2 = BigDecimal.ZERO
    BigDecimal aging3 = BigDecimal.ZERO
    BigDecimal aging4 = BigDecimal.ZERO
    BigDecimal aging5 = BigDecimal.ZERO
    BigDecimal aging6 = BigDecimal.ZERO
    BigDecimal aging7 = BigDecimal.ZERO
    BigDecimal aging8 = BigDecimal.ZERO
    BigDecimal aging9 = BigDecimal.ZERO
    BigDecimal aging10 = BigDecimal.ZERO

    //Performance metrics /KPI's
    BigDecimal avgDaysBt = BigDecimal.ZERO //avg days paid late / avg days beyond terms
    BigDecimal adp = BigDecimal.ZERO // avg days to pay
    BigDecimal adpWtd = BigDecimal.ZERO // weighted average days to pay
    BigDecimal cei = BigDecimal.ZERO //collection effectiveness index
    BigDecimal ddo = BigDecimal.ZERO //days deductions and disputes outstanding
    BigDecimal dso = BigDecimal.ZERO //day sales outstanding
    BigDecimal dsoad = BigDecimal.ZERO // DSO add back
    BigDecimal dsobp = BigDecimal.ZERO //DSO best possible
    BigDecimal dso12 = BigDecimal.ZERO //DSO using the last 12 month average

    String glPostPeriod

    //balance due fields
    BigDecimal curBal = BigDecimal.ZERO // whats not due yet
    BigDecimal maxDue = BigDecimal.ZERO // max totalDue in this month and last 12 months
    String maxDuePer = BigDecimal.ZERO // period the highest balance occured in the last 12 months
    BigDecimal pastDue = BigDecimal.ZERO //balance past due
    BigDecimal totalDue = BigDecimal.ZERO // total due, sum of all open items. will be the ending balance when month is closed

    //sales trending
    BigDecimal grossProfit = BigDecimal.ZERO// gross profit
    BigDecimal grossProfitPct = BigDecimal.ZERO// gross profit percent
    BigDecimal salesRolling12 = BigDecimal.ZERO
    // rolling 12 months of sales, TODO does this include current period?, is this just invoices?
    BigDecimal salesYtd = BigDecimal.ZERO //YTD sales

    //open trans docType
    BigDecimal openCM = BigDecimal.ZERO
    BigDecimal openDD = BigDecimal.ZERO
    BigDecimal openDM = BigDecimal.ZERO
    BigDecimal openIN = BigDecimal.ZERO
    BigDecimal openPA = BigDecimal.ZERO
    //openTotal is the same as totalDue

    //Roll forward measures
    BigDecimal beginBal = BigDecimal.ZERO //totalDue from prior period
    //created trans
    BigDecimal newCM = BigDecimal.ZERO
    BigDecimal newDD = BigDecimal.ZERO
    BigDecimal newDM = BigDecimal.ZERO
    BigDecimal newIN = BigDecimal.ZERO
    BigDecimal newPA = BigDecimal.ZERO
    BigDecimal newTotal = BigDecimal.ZERO //shortcut calc for the sum of the above

    //adjustments. used for roll forward
    BigDecimal adjusterAmt = BigDecimal.ZERO //the sum of origAmount of the ArAdjust.arTran
    BigDecimal adjustedTotal = BigDecimal.ZERO //the sum of arAdjustLine.amount
    /* maybe for future?
    BigDecimal adjustedCM
    BigDecimal adjustedDD
    BigDecimal adjustedDM
    BigDecimal adjustedIN
    BigDecimal adjustedPA
    */

    //flex fields for other open stats
    BigDecimal num1 = BigDecimal.ZERO
    BigDecimal num2 = BigDecimal.ZERO
    BigDecimal num3 = BigDecimal.ZERO
    BigDecimal num4 = BigDecimal.ZERO
    BigDecimal num5 = BigDecimal.ZERO
    BigDecimal num6 = BigDecimal.ZERO

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
