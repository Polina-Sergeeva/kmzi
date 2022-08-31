import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

public class DSA {
    protected String message; //сообщение
    protected String hash_md5; // хэш сообщения
    protected BigInteger G; // один из открытых парметров, используется для генерации откр ключа
    protected BigInteger Q; // простое число длины равной длине хэша. Делитель р - 1
    protected BigInteger P; // простое число, длины минимум 512
    protected BigInteger Y; // открытый ключ
    protected PairForDSA keys; // подпись

    protected void readFile(String fileName) throws IOException {
        FileInputStream file = new FileInputStream(fileName);
        File f = new File(fileName);
        byte[] message = new byte[(int)f.length()];
        file.read(message);

        this.message = new String(message);
    }
    protected String getMD5Hash(String data) throws NoSuchAlgorithmException {
        Security.addProvider(new BouncyCastleProvider());
        MessageDigest digest = MessageDigest.getInstance("MD5");
        byte[] encodedHash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < encodedHash.length; i++) {
            String hex = Integer.toHexString(0xff & encodedHash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }

        return hexString.toString();
    }
}
