# update item
curl -X PUT 'http://demo.9ci.io/api/payment/{id}' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {access-token}' \
  --data '{
  ...payload...
  }'
