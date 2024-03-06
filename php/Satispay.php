<?php

$bodyObject = [
    'flow' => 'MATCH_CODE',
    'amount_unit' => 100,
    'currency' => 'EUR'
];

$body = json_encode($bodyObject);

echo "body:\n";
echo $body . "\n";

$digest = "SHA-256=" . base64_encode(hash("sha256", $body, true));

echo "\ndigest:\n";
echo $digest . "\n";

$date = date('r');

echo "\ndate:\n";
echo $date . "\n";

$message = "(request-target): post /g_business/v1/payments
host: staging.authservices.satispay.com
date: $date
digest: $digest";

echo "\nmessage:\n";
echo $message . "\n";

$privateKey = file_get_contents('private.pem'); // your private key

echo "\nprivate.pem:\n";
echo $privateKey . "\n";

openssl_sign($message, $signatureRaw, $privateKey, OPENSSL_ALGO_SHA256);
$signature = base64_encode($signatureRaw);

echo "\nsignature:\n";
echo $signature . "\n";

$keyId = file_get_contents('KeyId.txt'); // your KeyId
$authorization = "Signature keyId=\"$keyId\", algorithm=\"rsa-sha256\", headers=\"(request-target) host date digest\", signature=\"$signature\"";

echo "\nauthorization:\n";
echo $authorization . "\n";

// send an HTTP request to the /g_business/v1/payments URL
// the body must be equals to the $body variable
// the $date variable must be sent as "date" header
// the $authorization variable must be sent as "authorization" header
