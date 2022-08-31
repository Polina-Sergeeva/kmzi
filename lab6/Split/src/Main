import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.Scanner;


public class Main {
    public static SecretShare[] split(final BigInteger secret, int needed, int available, BigInteger prime, Random random)
    {
        final BigInteger[] coeff = new BigInteger[needed];
        coeff[0] = secret;
        for (int i = 1; i < needed; i++)
        {
            BigInteger r;
            while (true)
            {
                r = new BigInteger(prime.bitLength(), random);
                if (r.compareTo(BigInteger.ZERO) > 0 && r.compareTo(prime) < 0)
                {
                    break;
                }
            }
            coeff[i] = r;
        }

        final SecretShare[] shares = new SecretShare[available];
        for (int x = 1; x <= available; x++)
        {
            BigInteger accum = secret;

            for (int exp = 1; exp < needed; exp++)
            {
                accum = accum.add(coeff[exp].multiply(BigInteger.valueOf(x).pow(exp).mod(prime))).mod(prime);
            }
            shares[x - 1] = new SecretShare(x, accum);
            System.out.println("Share " + shares[x - 1]);
        }

        return shares;
    }

    public static boolean isPrime(Long n)
    {
        for (Long i = 2L; i <= Math.sqrt(n); i++)
            if (n % i == 0)
                return false;
        return true;
    }


    public static void main(String[] args) {
        final SecureRandom random = new SecureRandom();
        Scanner scan = new Scanner(System.in);
        System.out.print("Введите секрет: ");
        final BigInteger secret;
        secret = scan.nextBigInteger();
        Long prime;
        int needed;
        int available;
        System.out.print("Введите кол-во точек: ");
        available = scan.nextInt();
        System.out.print("Введите кол-во участнков: ");
        needed = scan.nextInt();
        prime = secret.longValue() + 1;
        while (prime != 0)
        {
            if (isPrime(prime))
                break;
            else
                prime++;
        }
        System.out.printf("P: " + prime + "\n");
        final SecretShare[] shares = Main.split(secret, needed, available, BigInteger.valueOf(prime), random);

    }
}
