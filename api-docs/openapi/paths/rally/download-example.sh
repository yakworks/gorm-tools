# download a file, the 'some_file.zip' after the attachment id is optional here
# but useful for browser links as it gives a default name for saving
curl "https://sandbox.9ci.io/api/download/1234/some_file.zip" \
  -H 'Authorization: Bearer {access-token}' \
  --output some_file.zip
