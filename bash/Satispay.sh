#!/bin/bash

body='{
  "flow": "MATCH_CODE",
  "amount_unit": 100,
  "currency": "EUR"
}'

echo "body:"
printf "%s" "$body"

digest="SHA-256=$(printf "%s" "$body" | openssl dgst -sha256 -binary | base64 -w 0)"

echo "\n"
echo "\ndigest:"
echo "$digest"

date=$(date -R)

echo "\ndate:"
echo "$date"

message="(request-target): post /g_business/v1/payments"$'\n'
message+="host: staging.authservices.satispay.com"$'\n'
message+="date: $date"$'\n'
message+="digest: $digest"

echo "\nmessage:"
echo "$message"

private_key=$(cat private.pem)

echo "\nprivate.pem:"
echo "$private_key"

signature=$(printf "%s" "$message" | openssl dgst -sign private.pem -sha256 -binary | base64 -w 0)

echo "\nsignature:"
echo "$signature"

keyId=$(cat KeyId.txt)

authorization="Signature keyId=\"$keyId\", algorithm=\"rsa-sha256\", headers=\"(request-target) host date digest\", signature=\"$signature\""

echo "\nauthorization:"
echo "$authorization"

# send an HTTP request to the /g_business/v1/payments URL
# the body must be equal to the $body variable
# the $date variable must be sent as "date" header
# the $authorization variable must be sent as "authorization" header
