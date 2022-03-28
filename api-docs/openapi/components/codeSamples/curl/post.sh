# create item
curl -X put 'http://demo.9ci.io/api/payment/123' \
-H 'Content-Type: application/json' \
-H 'Authorization: Bearer {access-token}' \
--data '{
  webhookEnabled:true,
    payments:[
    {  imageUrl: "arbatch_images/1201/1.TIF" },
    {  imageUrl: "arbatch_images/1201/2.TIF"}
    ]}'

curl --header "Content-Type: application/json"   \
  --request POST \
  --data '{
  webhookEnabled:true,
  payments:[
  {  imageUrl: "arbatch_images/1201/1.TIF" },
  {  imageUrl: "arbatch_images/1201/2.TIF"}
  ]}' \
  http://127.0.0.1:8080/payment/?clientId=1
