import java.io.IOException;
import java.security.NoSuchAlgorithmException;


public class Main {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        DSAEncrypt dsa_enc = new DSAEncrypt(128, 512);
        dsa_enc.doSignature("C:\\kmzi\\file.txt");

        DSADecrypt dsa_dec = new DSADecrypt(dsa_enc.getP(), dsa_enc.getQ(), dsa_enc.getG(), dsa_enc.getY(), dsa_enc.getKeys());
        System.out.println(dsa_enc.getP());
        System.out.println(dsa_enc.getQ());
        System.out.println("Received signature is " + dsa_dec.checkSignature("C:\\kmzi\\file.txt"));

        dsa_enc.doFakeSignature();
        dsa_dec = new DSADecrypt(dsa_enc.getP(), dsa_enc.getQ(), dsa_enc.getG(), dsa_enc.getY(), dsa_enc.getKeys());
        System.out.println("Received signature is " + dsa_dec.checkSignature("C:\\kmzi\\file.txt"));


    }
}
