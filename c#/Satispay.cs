using System;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;
using System.Text.Json;

class Satispay
{
    static void Main(string[] args)
    {
        var bodyObject = new Dictionary<string, object>
        {
            { "flow", "MATCH_CODE" },
            { "amount_unit", 100 },
            { "currency", "EUR" }
        };

        var body = JsonSerializer.Serialize(bodyObject);

        Console.WriteLine("body:");
        Console.WriteLine(body);

        var digest = createDigest(body);

        Console.WriteLine();
        Console.WriteLine("digest:");
        Console.WriteLine(digest);

        string date = DateTime.UtcNow.ToString("r");

        Console.WriteLine();
        Console.WriteLine("date:");
        Console.WriteLine(date);

        string message = "(request-target): post /g_business/v1/payments\n" +
                         "host: staging.authservices.satispay.com\n" +
                        $"date: {date}\n" +
                        $"digest: {digest}";

        Console.WriteLine();
        Console.WriteLine("message:");
        Console.WriteLine(message);

        string privateKey = readPemKey("private.pem"); // your private key

        Console.WriteLine();
        Console.WriteLine("private.pem:");
        Console.WriteLine(privateKey);

        string signature = signData(message, privateKey);

        Console.WriteLine();
        Console.WriteLine("signature:");
        Console.WriteLine(signature);

        string keyId = readFile("KeyId.txt"); // your KeyId
        string authorization = $"Authorization: Signature keyId=\"{keyId}\", algorithm=\"rsa-sha256\", headers=\"(request-target) host date digest\", signature=\"{signature}\"";
        
        Console.WriteLine();
        Console.WriteLine("authorization:");
        Console.WriteLine(authorization);

        // send an HTTP request to the /g_business/v1/payments URL
        // the body must be equals to the body variable
        // the date variable must be sent as "date" header
        // the authorization variable must be sent as "authorization" header
    }

    static string createDigest(string body)
    {
        var sha256 = SHA256.Create();

        var hashBytes = sha256.ComputeHash(Encoding.UTF8.GetBytes(body));

        return "SHA-256=" + Convert.ToBase64String(hashBytes);
    }
    
    static string signData(string data, string privateKey)
    {
        byte[] privateKeyBytes = Convert.FromBase64String(privateKey);
        byte[] dataBytes = Encoding.UTF8.GetBytes(data);

        using (var rsa = RSA.Create())
        {
            rsa.ImportPkcs8PrivateKey(privateKeyBytes, out _);

            var sha256 = SHA256.Create();

            byte[] hash = sha256.ComputeHash(dataBytes);

            byte[] signatureBytes = rsa.SignHash(hash, HashAlgorithmName.SHA256, RSASignaturePadding.Pkcs1);

            string signature = Convert.ToBase64String(signatureBytes);

            return signature;
        }
    }
  
    static string readPemKey(string filePath)
    {
        string pemText = File.ReadAllText(filePath);

        string base64Key = pemText.Replace("-----BEGIN PRIVATE KEY-----", "")
                                  .Replace("-----END PRIVATE KEY-----", "")
                                  .Replace("\r", "")
                                  .Replace("\n", "");

        return base64Key;
    }
    
    static string readFile(string filePath)
    {
        return File.ReadAllText(filePath);
    }
}
