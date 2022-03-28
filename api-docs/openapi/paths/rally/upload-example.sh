# upload file
curl -X POST "https://sandbox.9ci.io/api/upload?name=big_file.zip" \
  -H "Content-Type: application/octet-stream" \
  -H 'Authorization: Bearer {access-token}' \
  --data-binary "@big_file_12345.zip"
