# correct
curl -X put 'http://demo.9ci.io/api/autocash/payment/rpc?op=correct' \
-H 'Content-Type: application/json' \
-H 'Authorization: Bearer {access-token}' \
--data '{
  ids:[123, 456]
  ...
}'

# reconcile
curl -X put 'http://demo.9ci.io/api/autocash/payment/rpc?op=reconcile' \
...
--data '{
  ids:[123, 456]
  glPostDate: 2022-02-28
  comments: (String) comments for reversal
  reasonId: (long) reason id to be set on artran pa
  ...
}'
