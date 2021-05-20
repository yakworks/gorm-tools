# create item
curl -X POST 'http://demo.9ci.io/api/payment' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {access-token}' \
  --data '{
  ...payload...
  }'
