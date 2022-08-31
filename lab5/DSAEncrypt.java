

import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.Random;


public class DSAEncrypt extends  DSA{
    private int length_p; // длина простого числа р
    private int length_S; // длина S, U, Q
    private BigInteger S; // случайная последовательность для генерации Q
    private BigInteger C; // один из параметров генерации P (счетчик)
    private BigInteger U; // видоизменный S
    private BigInteger X; // закрытый ключ

    public DSAEncrypt(int length_S, int length_p) throws NoSuchAlgorithmException {
        if(length_p %  64 == 0){
            this.length_p = length_p;
        } else {
            while(length_p %  64 != 0){
                length_p++;
            }
            this.length_p = length_p;
        }
        this.length_S = length_S;
        generatePandQ();
        generateG();
        generateSecretKeyX();
        generatePublicKeyY();
        //System.out.println(calculateInverseElem(BigInteger.valueOf(34), BigInteger.valueOf(13)));
    }

    private void generateS(){
        Random rand = new Random(System.currentTimeMillis());
        S = new BigInteger(length_S, rand);
        while(S.bitLength() != length_S) {
            S = new BigInteger(length_S, rand);
        }
    }
    private void generateU() throws NoSuchAlgorithmException {
        BigInteger MD4FromS = new BigInteger(getMD4Hash(S.toString()), 16);
        BigInteger twoPowG = AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(length_S)); //2^g
        BigInteger right = S.add(BigInteger.ONE).mod(twoPowG); // MD4(S+1)mod2^g
        BigInteger MD4FromSplus1 = new BigInteger(getMD4Hash(right.toString()), 16);
        U = MD4FromS.xor(MD4FromSplus1);
    }
    private void generateQ(){
        Q = U.or(BigInteger.ONE);
        Q = Q.or(BigInteger.ONE.shiftLeft(length_S - 1));
    }
    private void generatePandQ() throws NoSuchAlgorithmException {
        int n = (length_p - 1) / length_S; // L-1 = 128*n + b, b < 128, L - length_p
        int b = (length_p - 1) - n * length_S;

        Q = BigInteger.TEN; // просто так, чтобы инициализировать не простым числом
        while(!AlgorithmsDSA.checkPrime(Q)) {
            generateS();
            generateU();
            generateQ();
        }
        C = BigInteger.ZERO;
        BigInteger N = BigInteger.TWO;

        while(true) {
            BigInteger[] Vk = new BigInteger[n+1];
            for (int k = 0; k <= n; k++) {
                BigInteger tmp = S.add(N).add(BigInteger.valueOf(k));
                BigInteger tmp_pow = AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(length_S));
                Vk[k] = new BigInteger(getMD4Hash(tmp.mod(tmp_pow).toString()), 16);
            }

            BigInteger W = BigInteger.ZERO;
            for (int k = 0; k < n; k++) {
                W = W.add(AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(128 * k)).multiply(Vk[k]));
            }

            W = W.add(AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(128 * n))).multiply(
                    Vk[n].mod(AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(b))));

            BigInteger X = W.add(AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(length_p - 1)));
            P = X.subtract(X.mod(Q.multiply(BigInteger.TWO)).subtract(BigInteger.ONE));
            //System.out.println(pow(BigInteger.TWO, BigInteger.valueOf(length_p - 1)).toString(2));

            //System.out.println(X.mod(Q.multiply(BigInteger.TWO)).subtract(BigInteger.ONE));

            if (P.compareTo(AlgorithmsDSA.pow(BigInteger.TWO, BigInteger.valueOf(length_p - 1))) < 0 || !AlgorithmsDSA.checkPrime(P)) {
                C = C.add(BigInteger.ONE);
                N = N.add(BigInteger.valueOf(n)).add(BigInteger.ONE);
                if (C.equals(BigInteger.valueOf(4096))) {
                    generatePandQ();
                    return;
                } else{
                    continue;
                }
            }
            if(AlgorithmsDSA.checkPrime(P)){
                return;
            }
        }
    }

    private void generateG(){
        BigInteger h = BigInteger.TWO;
        while(true){
            BigInteger pow = P.subtract(BigInteger.ONE).divide(Q);
            G = AlgorithmsDSA.powMod(h, pow, P);
            if(!G.equals(BigInteger.ONE)){
                return;
            }
            do {
                Random rand = new Random(System.currentTimeMillis());
                h = new BigInteger(P.subtract(BigInteger.ONE).bitLength(), rand);
            } while (h.equals(BigInteger.ZERO));
        }
    }

    private void generateSecretKeyX(){
        Random rand = new Random(System.currentTimeMillis());
        X = new BigInteger(length_S - 1, rand);
        while (X.compareTo(Q) >= 0) {
            X = new BigInteger(length_S, rand);
        }
    }
    private void generatePublicKeyY(){
        Y = AlgorithmsDSA.powMod(G, X, P);
    }


    public BigInteger getP(){
        return P;
    }
    public BigInteger getQ() {
        return Q;
    }
    public BigInteger getG() {
        return G;
    }
    public BigInteger getY() {
        return Y;
    }
    public String getHash_md4() {
        return hash_md4;
    }
    public PairForDSA getKeys(){
        return keys;
    }
    public BigInteger getC() {
        return C;
    }
    public BigInteger getS(){ return S; }

    public PairForDSA doSignature(String fileName) throws NoSuchAlgorithmException, IOException {
        readFile(fileName);
        hash_md4 = getMD4Hash(message);

        while (true) {
            Random rand = new Random(System.currentTimeMillis());
            BigInteger k = new BigInteger(Q.bitLength(), rand);
            while(k.compareTo(Q) >= 0){
                k = new BigInteger(Q.bitLength(), rand);
            }

            BigInteger r = AlgorithmsDSA.powMod(G, k, P).mod(Q);
            while (r.equals(BigInteger.ZERO)) {
                k = new BigInteger(Q.bitLength(), rand);
                r = AlgorithmsDSA.powMod(G, k, P).mod(Q);
            }

            hash_md4 = getMD4Hash(message);
            BigInteger hashMessage = new BigInteger(hash_md4, 16);
            BigInteger tmp = X.multiply(r).add(hashMessage);
            BigInteger s = AlgorithmsDSA.calculateInverseElem(k, Q).multiply(tmp).mod(Q);
            if (!s.equals(BigInteger.ZERO)) {
                //System.out.println(r + " " + s);
                keys = new PairForDSA(r, s);
                return keys;
            }
        }
    }

    public PairForDSA doFakeSignature() {
        BigInteger tmp = keys.getR().add(BigInteger.TWO);
        keys.setR(tmp);

        tmp = keys.getS().add(BigInteger.TWO);
        keys.setS(tmp);
        return keys;
    }

}
