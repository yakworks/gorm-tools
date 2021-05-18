#simple query
curl -i -G 'http://localhost:8080/api/payment?q=123&sort=refnum&page=2&max=5'

# query with mango, use -G for the get to append the data-urlencode params, 
# -v shows trace to see resulting url
curl -i -v -G 'http://localhost:8080/api/org?page=2' \
  --data-urlencode 'q={refnum:"1234*"}'