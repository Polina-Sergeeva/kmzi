import java.math.BigInteger;
import java.util.Random;

public class AlgorithmsDSA {
    public static BigInteger powMod(BigInteger number, BigInteger pow, BigInteger mod){
        BigInteger res = BigInteger.valueOf(1);
        int n = pow.bitLength();
        BigInteger[] array = new BigInteger[n];

        array[0] = number;
        for(int i = 1; i < n; i++){
            array[i] = array[i - 1].pow(2).mod(mod);
        }
        for(int i = 0; i < n; i++){
            if(pow.testBit(i)){
                res = res.multiply(array[i]);
            }
        }

        res = res.mod(mod);

        return res;
    }
    public static BigInteger pow(BigInteger num, BigInteger pow) {
        if(pow.equals(BigInteger.ZERO)){
            return BigInteger.ONE;
        }
        if(pow.mod(BigInteger.TWO).equals(BigInteger.ZERO)){
            return pow(num.multiply(num), pow.divide(BigInteger.TWO));
        }
        return num.multiply(pow(num, pow.subtract(BigInteger.ONE)));
    }
    public static BigInteger calculateInverseElem(BigInteger num, BigInteger mod){
        return powMod(num, mod.subtract(BigInteger.TWO), mod);
    }

    private static BigInteger calculateB(BigInteger p){ // сколько раз р-1 делится на 2
        BigInteger b = BigInteger.ZERO;
        p = p.subtract(BigInteger.ONE);

        BigInteger tmp = p.remainder(AlgorithmsDSA.pow(BigInteger.TWO, b));
        while(tmp.equals(BigInteger.ZERO)){
            tmp = p.remainder(AlgorithmsDSA.pow(BigInteger.TWO, b));
            p = p.divide(AlgorithmsDSA.pow(BigInteger.TWO, b));
            b = b.add(BigInteger.ONE);
        }
        return b;
    }
    private static BigInteger calculateM(BigInteger b, BigInteger q){
        return q.divide(AlgorithmsDSA.pow(BigInteger.TWO, b));
    }
    private static long generateAForCheckPrime(BigInteger p){
        Random rand = new Random(System.currentTimeMillis());
        long a = rand.nextLong();
        while (!(BigInteger.valueOf(a).compareTo(p) < 0)) {
            a = rand.nextLong();
        }
        return a;
    }
    public static boolean checkPrime(BigInteger p){
        BigInteger b = calculateB(p); // b - сколько раз число p-1 делится на 2
        BigInteger m = calculateM(b, p.subtract(BigInteger.ONE)); // p = 1 + 2^b * m

        for(int i = 0; i < 5; i++) {
            long a = generateAForCheckPrime(p);

            BigInteger z = BigInteger.valueOf(a);
            z = AlgorithmsDSA.powMod(z, m, p);
            for(int j = 0; ; ) {
                //System.out.println(z.modPow(m, p).toString());
                //System.out.println(powMod(z, m, p).toString());

                if (z.equals(p.subtract(BigInteger.ONE)) || z.equals(BigInteger.ONE)) {
                    break;
                }
                if (j > 0 && z.equals(BigInteger.ONE)) {
                    return false;
                }
                j++;
                if(j < b.longValue() && !z.equals(p.subtract(BigInteger.ONE))){
                    z = AlgorithmsDSA.powMod(z, BigInteger.TWO, p);
                    continue;
                }
                if(z.equals(p.subtract(BigInteger.ONE))){
                    break;
                }
                else if(BigInteger.valueOf(j).equals(b) && !z.equals(p.subtract(BigInteger.ONE))){
                    return false;
                }
            }
        }

        return true;
    }
}
