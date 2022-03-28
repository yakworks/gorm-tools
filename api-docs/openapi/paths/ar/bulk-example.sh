# call bulk with attachmentId
curl -X POST "https://sandbox.9ci.io/api/ar/tran/bulk?detailLinkField=arTran_sourceId&headerPathDelimiter=_&attachmentId=1234&controlCount=1000" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

# call bulk with json body
curl -X POST "https://sandbox.9ci.io/api/ar/tran/bulk?asyncEnabled=true&jobSource=Oracle"\
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

[
  {
    "amount": 100,
    "refnum": "123",
    "customer": {
      "sourceId": "510100"
    }
  },
  {
    "amount": 200,
    "refnum": "234",
    "customer": {
      "sourceId": "K14700"
    }
  }
]

# RESPONSE

# call with asyncEnabled that returns job id
HTTP/1.1 207
{
  "id": 1000,
  "ok": false,
  "state": "Running",
  "sourceId": "POST /api/ar/tran/bulk?asyncEnabled=true",
  "data": []
}

