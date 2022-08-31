import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class DSADecrypt extends DSA {
    private BigInteger Ufirst;
    private BigInteger Usecond;
    private BigInteger V;
    private BigInteger W;

    public DSADecrypt(BigInteger P, BigInteger Q, BigInteger G, BigInteger Y, PairForDSA keys){
        this.P = P;
        this.Q = Q;
        this.G = G;
        this.Y = Y;
        this.keys = keys;
    }

    private void calculateW(){
        W = AlgorithmsDSA.calculateInverseElem(keys.getS(), Q);
    }
    private void calculateUfirst(){
        BigInteger tmp = new BigInteger(hash_md5, 16);
        Ufirst = tmp.multiply(W).mod(Q);
    }
    private void calculateUsecond(){
        Usecond = keys.getR().multiply(W).mod(Q);
    }
    private void calculateV(){
        BigInteger firstTmp = AlgorithmsDSA.powMod(G, Ufirst, P);
        BigInteger secondTmp = AlgorithmsDSA.powMod(Y, Usecond, P);

        V = firstTmp.multiply(secondTmp).mod(P).mod(Q);
    }


    public boolean checkSignature(String fileName) throws IOException, NoSuchAlgorithmException {
        readFile(fileName);
        this.hash_md5 = getMD5Hash(message);
        calculateW();
        calculateUfirst();
        calculateUsecond();
        calculateV();
        return V.equals(keys.getR());
    }
}
