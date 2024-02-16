import json
import hashlib
import base64
import time
import datetime
from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import serialization
from cryptography.hazmat.primitives.asymmetric import padding
from cryptography.hazmat.primitives import hashes

body_object = {
    'flow': 'MATCH_CODE',
    'amount_unit': 100,
    'currency': 'EUR'
}

body = json.dumps(body_object)

print("body:")
print(body)

digest = "SHA-256=" + base64.b64encode(hashlib.sha256(body.encode()).digest()).decode()

print()
print("digest:")
print(digest)

date = datetime.datetime.now(datetime.timezone.utc).strftime('%a, %d %b %Y %H:%M:%S GMT')

print()
print("date:")
print(date)

message = "(request-target): post /g_business/v1/payments\n\
host: staging.authservices.satispay.com\n\
date: " + date + "\n\
digest: " + digest

print()
print("message:")
print(message)

with open('private.pem', 'rb') as private_key_file:
    private_key_file = private_key_file.read()

    private_key = serialization.load_pem_private_key(
        private_key_file,
        password=None,
        backend=default_backend()
    )

    print()
    print("private.pem:")
    print(private_key_file.decode())

signature = private_key.sign(
    message.encode(),
    padding.PKCS1v15(),
    hashes.SHA256()
)

signature = base64.b64encode(signature).decode()

print()
print("signature:")
print(signature)

with open('KeyId.txt', 'r') as key_id_file:
    key_id = key_id_file.read().strip()

authorization = f'Signature keyId="{key_id}", algorithm="rsa-sha256", headers="(request-target) host date digest", signature="{signature}"'

print()
print("authorization:")
print(authorization)

# Send an HTTP request to the /g_business/v1/payments URL
# The body must be equal to the 'body' variable
# The 'date' variable must be sent as the "date" header
# The 'authorization' variable must be sent as the "authorization" header
