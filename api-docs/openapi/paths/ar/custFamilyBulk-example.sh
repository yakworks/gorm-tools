### CREATE

# create custFamily knowing id of child and parent
curl -X POST "https://sandbox.9ci.io/api/ar/custFamily/bulk" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

[
  {
    "child": {
      "id": 1234
    },
    "parent": {
      "id": 2345
    }
  }
]

# create custFamily with child sourceId and parent sourceid
curl -X POST "https://sandbox.9ci.io/api/ar/custFamily/bulk" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

[
  {
    "child": {
      "sourceId": "Cust-123-123"
    },
    "parent": {
      "sourceId": "Cust-234-234"
    }
  }
]


### UPDATES

# set new parent knowing child and parent ids
curl -X PUT "https://sandbox.9ci.io/api/ar/custFamily/bulk" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

[
  {
    "child": {
      "id": 1234
    },
    "parent": {
      "id": 2345
    }
  }
]

# set new parent with child sourceId and new parent sourceid
curl -X PUT "https://sandbox.9ci.io/api/ar/custFamily/bulk" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

[
  {
    "child": {
      "sourceId": "Cust-123-123"
    },
    "parent": {
      "sourceId": "Cust-234-234"
    }
  }
]

# remove parent, lookup cust family by child sourceId
curl -X PUT "https://sandbox.9ci.io/api/ar/custFamily/bulk" \
  -H "Content-Type: application/json" \
  -H 'Authorization: Bearer {access-token}'

[
  {
    "child": {
      "sourceId": "Cust-123-123"
    }
  }
]
