# call exportSync for single division
curl -X POST "https://sandbox.9ci.io/api/ar/exportSync?q={member.divisionId: (106)}" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'
# call exportSync for list of divisions
curl -X POST "https://sandbox.9ci.io/api/ar/exportSync?q={member.divisionId: (106, 107)}" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

# RESPONSE

# call defaults to asyncEnabled so job id is returned
HTTP/1.1 207
{
  "id": 1000,
  "ok": false,
  "state": "Running",
  "sourceId": "POST /api/ar/exportSync?q={member.divisionId: (106)}",
  "data": []
}

# RESPONSE WITH PROCESSED DATA:

# call JobId to receive processed data
HTTP/1.1 207
{
  "id": 1000,
  "state": "Finished",
  "ok": true ,
  "state": "Finished",
  "sourceId": "POST /api/ar/exportSync?q={member.divisionId: (106)}",
  "data": [
    {  ### First payment, pays 1 invoice and creates CM (2 new ArTrans CM and oDM)
        "trans": [
            {
                "id": 616,
                "createdDate": "2009-07-19T17:31:21",
                "term": {
                    "id": 12
                },
                "discAmount": 0.0000,
                "dueDate": "2009-07-19",
                "editedDate": "2009-07-19T17:31:21",
                "version": 1,
                "tranType": {
                    "id": 30
                },
                "state": 0,
                "currency": "USD",
                "member": {
                    "id": 616,
                    "branch": {
                        "num": "Chi"
                    }
                },
                "tranDate": "2009-07-19",
                "glAcct": "20110-000",
                "editedBy": 107,
                "docType": "CM",
                "companyId": 2,
                "ext": {
                    "id": 616,
                    "bolnum:": 123
                },
                "glPostDate": "2009-07-19",
                "refnum": "1000",
                "flex": {
                    "id": 616,
                    "text1": "test"
                },
                "customer": {
                    "id": 205,
                    "num": "K14700"
                },
                "autoCash": {
                    "id": 616,
                    "createdByPayId": 619
                },
                "source": {
                    "id": 616,
                    "sourceId": 616,
                    "sourceType": "App"
                },
                "ponum": "cm_test",
                "related": {
                    "id": 616
                },
                "glPostPeriod": "200612",
                "createdArBatchId": 10168,
                "origAmount": -10.0000,
                "createdBy": 107,
                "status": {
                    "id": 1
                },
                "amount": -10.0000,
                "dispute": {
                    "id": 616
                },
                "custNum": "K14700"
            },
            {
                "id": 622,
                "createdDate": "2009-07-19T17:31:21",
                "term": {
                    "id": 12
                },
                "discAmount": 0.0000,
                "dueDate": "2009-07-19",
                "editedDate": "2009-07-19T17:31:21",
                "version": 1,
                "tranType": {
                    "id": 42
                },
                "state": 1,
                "currency": "USD",
                "member": {
                    "id": 622,
                    "branch": {
                        "num": "Chi"
                    }
                },
                "tranDate": "2009-07-19",
                "glAcct": "11010-003",
                "closedDate": "2009-07-19T17:31:22",
                "editedBy": 107,
                "docType": "DM",
                "companyId": 2,
                "glPostDate": "2009-07-19",
                "refnum": "1006",
                "customer": {
                    "id": 205,
                    "num": "K14700"
                },
                "autoCash": {
                    "id": 622
                },
                "source": {
                    "id": 622,
                    "sourceId": 622,
                    "sourceType": "App"
                },
                "ponum": "cm_test",
                "related": {
                    "id": 622
                },
                "glPostPeriod": "200612",
                "origArTranId": 616,
                "createdArBatchId": 10168,
                "origAmount": 10.0000,
                "createdBy": 107,
                "status": {
                    "id": 5
                },
                "amount": 0.0000,
                "custNum": "K14700"
            }
        ],
        "adjust": {
            "id": 4,
            "createdDate": "2009-07-19T17:31:22",
            "glPostDate": "2009-07-19",
            "arTran": {
                "id": 619,
                "createdDate": "2009-07-19T17:31:21",
                "discAmount": 0.0000,
                "dueDate": "2009-07-19",
                "editedDate": "2009-07-19T17:31:21",
                "version": 1,
                "tranType": {
                    "id": 20
                },
                "state": 1,
                "tranDate": "2009-07-19",
                "docType": "PA",
                "companyId": 2,
                "glPostDate": "2009-07-19",
                "refnum": "pa",
                "customer": {
                    "id": 205,
                    "num": "K14700"
                },
                "source": {
                    "id": 619,
                    "sourceId": 619,
                    "sourceType": "App"
                },
                "ponum": "cm_test",
                "related": {
                    "id": 616
                },
                "glPostPeriod": "200612",
                "createdArBatchId": 10168,
                "origAmount": -10.0000,
                "createdBy": 107,
                "status": {
                    "id": 1
                },
                "amount": -10.0000,
                "dispute": {
                    "id": 616
                },
                "custNum": "K14700"
            },
            "editedDate": "2009-07-19T17:31:22",
            "version": 0,
            "state": 1,
            "source": "RCM",
            "glPostPeriod": "200907",
            "createdBy": 107,
            "jobId": 3550,
            "editedBy": 1,
            "arPostDate": "2009-07-19",
            "arBatchId": 10168,
            "lines": [
                {
                    "amount": 10,
                    "arTran": {
                        "id": 622,
                        "source": {
                            "sourceId": 622,
                            "sourceType": "App"
                        }
                    },
                    "discAmount": 0,
                    "id": 954
                },
                {
                    "amount": 257.4600,
                    "arTran": {
                        "id": 464,
                        "source": {
                            "sourceId": "IN18869",
                            "sourceType": "ERP"
                        }
                    },
                    "discAmount": 0,
                    "id": 954
                }
            ]
        }
    },
    {
      "trans": [ ],
      "adjust": {"id":123,"arTran":..., "lines":[ {..},{..}]}
    },
    {  ####### another payment, no new trans created, just PA to existing INs
      "adjust": {"id":,"arTran":..., "lines":[ {..},{..}]}}

    },
    { ####### another payment, no adjustments, unapplied only
      "trans": [
        {
          "id": 6,
          "refnum": "Chk123",
          "amount": 100,
          "source": {
            "sourceId": "3",
            "sourceType": "App"
          },
          "tranType": {
            "code": "PA"
          }
        }
      ]
    }
  ]
}


# CALL UPDATE JOB THAT DATA WAS RECEIVED SUCCESSFULLY:

curl -X PUT "https://sandbox.9ci.io/api/rally/syncJob/{id}" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'
{
  "state": "Finished"
}
