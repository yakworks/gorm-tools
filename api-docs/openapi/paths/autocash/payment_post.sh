# update
curl -X put 'http://demo.9ci.io/api/autocash/payment/123' \
-H 'Content-Type: application/json' \
-H 'Authorization: Bearer {access-token}' \
--data '{
  "amount": 99.99
  ...
}'

# Add or Remove ArTrans
--data '{
  trans: [
    {"op": "remove", "ids": [123,124]},
    {"op": "add",    "ids": [221,223]},
  ]
}'

# update with empty array to remove all payment details
# cann also be done with rpc removeDetail
--data '{
  detail: {
    "op": "update", data: []
  }
}
