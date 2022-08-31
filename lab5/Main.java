

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import dsa.DSADecrypt;
import dsa.DSAEncrypt;

public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        DSAEncrypt dsa_enc = new DSAEncrypt(128, 512);
        dsa_enc.doSignature("C:\\kmzi\\lab5.1\\file.txt");

        DSADecrypt dsa_dec = new DSADecrypt(dsa_enc.getP(), dsa_enc.getQ(), dsa_enc.getG(), dsa_enc.getY(), dsa_enc.getKeys());
        System.out.println("Received signature is " + dsa_dec.checkSignature("C:\\kmzi\\lab5.1\\file.txt"));

        dsa_enc.doFakeSignature();
        dsa_dec = new DSADecrypt(dsa_enc.getP(), dsa_enc.getQ(), dsa_enc.getG(), dsa_enc.getY(), dsa_enc.getKeys());
        System.out.println("Received signature is " + dsa_dec.checkSignature("C:\\kmzi\\lab5.1\\file.txt"));
    }
}
