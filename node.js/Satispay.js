const crypto = require('crypto');
const fs = require('fs');

const bodyObject = {
    flow: 'MATCH_CODE',
    amount_unit: 100,
    currency: 'EUR'
};

const body = JSON.stringify(bodyObject);

console.log("\nbody:");
console.log(body);

const digest = "SHA-256=" + crypto.createHash('sha256').update(body).digest('base64');

console.log("\ndigest:");
console.log(digest);

const date = new Date().toUTCString();

console.log("\ndate:");
console.log(date);

const message = "(request-target): post /g_business/v1/payments\n" +
    "host: staging.authservices.satispay.com\n" +
    "date: " + date + "\n" +
    "digest: " + digest;

console.log("\nmessage:");
console.log(message);

const privateKey = fs.readFileSync('private.pem', 'utf-8');

console.log("\nprivate.pem:");
console.log(privateKey);

const sign = crypto.createSign('RSA-SHA256');
sign.update(message);
const signatureRaw = sign.sign(privateKey, 'base64');
const signature = Buffer.from(signatureRaw, 'base64').toString('base64');

console.log("\nsignature:");
console.log(signature);

const keyId = fs.readFileSync('KeyId.txt', 'utf-8');
const authorization = "Signature keyId=\"" + keyId.trim() + "\", algorithm=\"rsa-sha256\", headers=\"(request-target) host date digest\", signature=\"" + signature + "\"";

console.log("\nauthorization:");
console.log(authorization);

// send an HTTP request to the /g_business/v1/payments URL
// the body must be equals to the $body variable
// the date variable must be sent as "date" header
// the authorization variable must be sent as "authorization" header