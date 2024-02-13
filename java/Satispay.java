import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat.Encoding;

import org.json.JSONObject;

public class Satispay {
    public static void main(String[] args) {
        JSONObject bodyObject = new JSONObject()
            .put("flow", "MATCH_CODE")
            .put("amount_unit", 100)
            .put("currency", "EUR");

        String body = bodyObject.toString();

        System.out.println("body:");
        System.out.println(body);

        String digest = createDigest(body);

        System.out.println();
        System.out.println("digest:");
        System.out.println(digest);

        SimpleDateFormat sdf = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
        String date = sdf.format(new Date());
        
        String message = "(request-target): post /g_business/v1/payments\n" +
                         "host: staging.authservices.satispay.com\n" +
                         "date: " + date + "\n" +
                         "digest: " + digest;


        System.out.println();
        System.out.println("message:");
        System.out.println(message);

        String privateKey = readPemKey("private.pem"); // your private key

        System.out.println();
        System.out.println("private.pem:");
        System.out.println(privateKey);

        String signature = signData(message, privateKey);

        String keyId = readFile("KeyId.txt");
        String authorization = String.format("Signature keyId=\"%s\", algorithm=\"rsa-sha256\", headers=\"(request-target) host date digest\", signature=\"%s\"", keyId, signature);
    
        System.out.println();
        System.out.println("authorization:");
        System.out.println(authorization);

        // send an HTTP request to the /g_business/v1/payments URL
        // the body must be equals to the body variable
        // the date variable must be sent as "date" header
        // the authorization variable must be sent as "authorization" header
    }

    private static String createDigest(String body) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = messageDigest.digest(body.getBytes("UTF-8"));
        
            return "SHA-256=" + Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public static String signData(String message, String base64PrivateKey) {
        try {

            byte[] privateKeyBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signatureBytes);
        } catch (
            NoSuchAlgorithmException |
            InvalidKeySpecException |
            InvalidKeyException |
            SignatureException e
        ) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    private static String readPemKey(String filePath) {
        try {
            String pemText = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

            Pattern pattern = Pattern.compile("-----(?:.*?)-----([^-]*)-----(?:.*?)-----", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(pemText);
        
            String base64Key = pemText;

            if (matcher.find()) {
                base64Key = matcher.group(1)
                                        .replace("\r", "")
                                        .replace("\n", "");;
            }
    
            return base64Key;
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        return null;
    }

    private static String readFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        return null;
    }
}