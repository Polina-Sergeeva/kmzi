
import java.math.BigInteger;

public class PairForDSA {
    private BigInteger r;
    private BigInteger s;

    public PairForDSA(BigInteger r, BigInteger s){
        this.r = r;
        this.s = s;
    }

    public BigInteger getR() {
        return r;
    }
    public BigInteger getS() {
        return s;
    }

    public void setR(BigInteger value){
        r = value;
    }
    public void setS(BigInteger value) {
        this.s = value;
    }
}
