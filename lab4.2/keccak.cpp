using System;
using System.Linq;
using System.Collections.Generic;

class Program
{
    /*const */
    static private Byte InstanceNumber = 6;
    /*const */
    static private UInt16[] b_array = { 25, 50, 100, 200, 400, 800, 1600 }; //permutation_width
    /*const */
    static private Byte matrixSize = 5 * 5;
    /*const */
    static private Byte[] w_array = { 1, 2, 4, 8, 16, 32, 64 }; //b / 25;
    /*const */
    static private Byte[] l_array = { 0, 1, 2, 3, 4, 5, 6 }; //log(static_cast<float>(m_w)) / log(2.0F)
    /*const */
    static private Byte[] n_array = { 12, 14, 16, 18, 20, 22, 24 }; //12 + 2 * l -> number_of_permutation
    /*const */
    static private Byte[,] r = {
        {0, 36, 3, 41, 18},
        {1, 44, 10, 45, 2},
        {62, 6, 43, 15, 61},
        {28, 55, 25, 21, 56},
        {27, 20, 39, 8, 14}
    };
    /*const */
    static private UInt64[] RC = {//24 by w_array[KeccakInstanceCount - 1] (for 1600), for less please use less in w_array
        0x0000000000000001,
        0x0000000000008082,
        0x800000000000808A,
        0x8000000080008000,
        0x000000000000808B,
        0x0000000080000001,
        0x8000000080008081,
        0x8000000000008009,
        0x000000000000008A,
        0x0000000000000088,
        0x0000000080008009,
        0x000000008000000A,
        0x000000008000808B,
        0x800000000000008B,
        0x8000000000008089,
        0x8000000000008003,
        0x8000000000008002,
        0x8000000000000080,
        0x000000000000800A,
        0x800000008000000A,
        0x8000000080008081,
        0x8000000000008080,
        0x0000000080000001,
        0x8000000080008008
    };

    static private UInt64[,] B = new UInt64[5, 5];
    static private UInt64[] C = new UInt64[5];
    static private UInt64[] D = new UInt64[5];

    /*const*/
    static public UInt16[] rate_array = { 576, 832, 1024, 1088, 1152, 1216, 1280, 1344, 1408 };
    /*const*/
    static public UInt16[] capacity_array = { 1024, 768, 576, 512, 448, 384, 320, 256, 192 };
    public enum SHA3 { SHA512 = 0, SHA384, SHA256 = 3, SHA224 };

    static private UInt64[,] Keccak_f(UInt64[,] A)
    {
        for(Byte i = 0; i < n_array[InstanceNumber]; i++)
            A = Round(A, RC[i]);
        return A;
    }

    static private UInt64[,] Round(UInt64[,] A, UInt64 RC_i)
    {
        Byte i, j;

        //theta step
        for (i = 0; i < 5; i++)
            C[i] = A[i,0] ^ A[i,1] ^ A[i,2] ^ A[i,3] ^ A[i,4];
        for (i = 0; i < 5; i++)
            D[i] = C[(i + 4) % 5] ^ ROT(C[(i + 1) % 5], 1, w_array[InstanceNumber]);
        for (i = 0; i < 5; i++)
            for (j = 0; j < 5; j++)
                A[i,j] = A[i,j] ^ D[i];

        //rho and pi steps
        for (i = 0; i < 5; i++)
            for (j = 0; j < 5; j++)
                B[j,(2 * i + 3 * j) % 5] = ROT(A[i,j], r[i,j], w_array[InstanceNumber]);

        //chi step
        for (i = 0; i < 5; i++)
            for (j = 0; j < 5; j++)
                A[i,j] = B[i,j] ^ ((~B[(i + 1) % 5,j]) & B[(i + 2) % 5,j]);

        //iota step
        A[0,0] = A[0,0] ^ RC_i;

        return A;
    }

    static private UInt64 ROT(UInt64 x, Byte n, Byte w)
    {
        return ((x << (n % w)) | (x >> (w - (n % w))));
    }

    static private Byte[] Keccak(UInt16 rate, UInt16 capacity, List<Byte> Message)
    {
        //Padding
        Message.Add(0x01);

        UInt16 min = (UInt16)((rate - 8) / 8);
        UInt16 n = (UInt16)Math.Truncate((Double)(Message.Count / min));
        UInt32 messageFullCount = 0;
        if (n < 2)
        {
            messageFullCount = min;
        }
        else
        {
            messageFullCount = (UInt32)(n * min + (n - 1));
        }

        UInt32 delta = (UInt32)(messageFullCount - Message.Count);
        if ((Message.Count + delta) > UInt16.MaxValue - 1)
            throw (new Exception("Message might be too large"));

        /*Byte[] byteArrayToAdd = new Byte[delta];
        Message.AddRange(byteArrayToAdd);*/

        while (delta > 0)
        {
            Message.Add(0x00);
            delta--;
        }

        if ((Message.Count * 8 % rate) != (rate - 8))
            throw (new Exception("Length was incorect calculated"));

        Message.Add(0x80);
        /*const*/
        Int32 size = (Message.Count * 8) / rate;

        UInt64[] P = new UInt64[size * matrixSize];
        Int32 xF = 0, count = 0;
        Byte i = 0, j = 0;

        for(xF = 0; xF < Message.Count; xF++)
        {
            if (j > (rate / w_array[InstanceNumber] - 1))
            {
                j = 0;
                i++;
            }
            count++;
            if ((count * 8 % w_array[InstanceNumber]) == 0)
            {
                P[size * i + j] = ReverseEightBytesAndToUInt64(
                    Message.GetRange(count - w_array[InstanceNumber] / 8, 8).ToArray()
                    );
                j++;
            }
        }

        //Initialization
        UInt64 [,]S = new UInt64[5,5];
        for(i = 0; i < 5; i++)
            for(j = 0; j < 5; j++)
                S[i,j] = 0;

        //Absorting phase
        for(xF = 0; xF < size; xF++)
        {
            for(i = 0; i < 5; i++)
                for(j = 0; j < 5; j++)
                    if ((i + j * 5) < (rate / w_array[InstanceNumber]))
                    {
                        S[i, j] = S[i, j] ^ P[size * xF + i + j * 5];
                    }
            Keccak_f(S);
        }

        //Squeezing phaze
        Byte a = 0;
        Byte d_max = (Byte)(capacity / (2 * 8));
        List<Byte> retHash = new List<Byte>(d_max);

        for( ; ; )
        {
            for(i = 0; i < 5; i++)
                for(j = 0; j < 5; j++)
                    if((5 * i + j) < (rate / w_array[InstanceNumber]))
                    {
                        if(a >= d_max)
                            i = j = 5;
                        else
                        {
                            retHash.AddRange(FromUInt64ToReverseEightBytes(S[j, i]));
                            a = (Byte)retHash.Count;
                        }
                    }
            if(a >= d_max)
                break;
            Keccak_f(S);
        }

        return retHash.GetRange(0, d_max).ToArray();
    }

    static private UInt64 ReverseEightBytesAndToUInt64(Byte[] bVal)
    {
        UInt64 ulVal = 0L;
        for (Byte i = 8, j = 0; i > 0; i--)
        {
            ulVal += (UInt64)((bVal[i - 1] & 0xF0) >> 4) * (UInt64)Math.Pow(16.0F, 15 - (j++));
            ulVal += (UInt64)(bVal[i - 1] & 0x0F) * (UInt64)Math.Pow(16.0F, 15 - (j++));
        }
        return ulVal;
    }

    static private Byte[] FromUInt64ToReverseEightBytes(UInt64 ulVal)
    {
        Byte[] bVal = new Byte[8];
        Byte a = 0;
        do
        {
            bVal[a] = (Byte)((ulVal % 16) * 1);
            ulVal = ulVal / 16;
            bVal[a] += (Byte)((ulVal % 16) * 16);
            a++;
        }
        while (15 < (ulVal = ulVal / 16));
        while (a < 8)
        {
            bVal[a++] = (Byte)ulVal;
            ulVal = 0;
        }

        return bVal;
    }

    static void Main(String[] args)
    {
        if (args.Length < 1)
            return;
        List<Byte> MessageB;
        String message = string.Copy(args[0]);

        MessageB = strToByteList(message);
        String hash_224 = ByteArrayToString(Keccak(rate_array[(Byte)SHA3.SHA224], capacity_array[(Byte)SHA3.SHA224], MessageB));
        MessageB = strToByteList(message);
        String hash_256 = ByteArrayToString(Keccak(rate_array[(Byte)SHA3.SHA256], capacity_array[(Byte)SHA3.SHA256], MessageB));
        MessageB = strToByteList(message);
        String hash_384 = ByteArrayToString(Keccak(rate_array[(Byte)SHA3.SHA384], capacity_array[(Byte)SHA3.SHA384], MessageB));
        MessageB = strToByteList(message);
        String hash_512 = ByteArrayToString(Keccak(rate_array[(Byte)SHA3.SHA512], capacity_array[(Byte)SHA3.SHA512], MessageB));

        Console.WriteLine("Message: " + message + "\r\n" +
                  "Hash_224: " + hash_224 + "\r\n" +
                  "Hash_256: " + hash_256 + "\r\n" +
                  "Hash_384: " + hash_384 + "\r\n" +
                  "Hash_512: " + hash_512 + "\r\n");
    }

    static List<Byte> strToByteList(String str)
    {
        List<Byte> ret = new List<byte>(str.Length);

        foreach(char ch in str)
        {
            ret.Add((Byte)ch);
        }

        return ret;
    }

    static public String ByteArrayToString(Byte[] b)
    {
        System.Text.StringBuilder sb = new System.Text.StringBuilder(16);
        for (Int32 i = 0; i < Math.Min(b.Length, Int32.MaxValue - 1); i++)
            sb.Append(String.Format("{0:X2}", b[i]));
        return sb.ToString();
    }
}