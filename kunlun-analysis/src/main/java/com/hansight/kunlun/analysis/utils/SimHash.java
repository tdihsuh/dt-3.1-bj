package com.hansight.kunlun.analysis.utils;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.Iterator;


/**
 * Created by justinwan on 14-7-8.
 */
public class SimHash {
    private static int hashbits = 64;


    public static BigInteger simHash(Iterator<String> tokens) throws IOException {
        int[] v = new int[hashbits];

        while (tokens.hasNext()) {
            String word = tokens.next();
            // 注意停用词会被干掉
            // System.out.println(word);
            // 2、将每一个分词hash为一组固定长度的数列.比如 64bit 的一个整数.
            BigInteger t = hash(word);
            for (int i = 0; i < hashbits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                // 3、建立一个长度为64的整数数组(假设要生成64位的数字指纹,也可以是其它数字),
                // 对每一个分词hash后的数列进行判断,如果是1000...1,那么数组的第一位和末尾一位加1,
                // 中间的62位减一,也就是说,逢1加1,逢0减1.一直到把所有的分词hash数列全部判断完毕.
                if (t.and(bitmask).signum() != 0) {
                    // 这里是计算整个文档的所有特征的向量和
                    // 这里实际使用中需要 +- 权重，比如词频，而不是简单的 +1/-1，
                    v[i] += 1;
                } else {
                    v[i] -= 1;
                }
            }
        }

        BigInteger fingerprint = new BigInteger("0");
        StringBuffer simHashBuffer = new StringBuffer();
        for (int i = 0; i < hashbits; i++) {
            // 4、最后对数组进行判断,大于0的记为1,小于等于0的记为0,得到一个 64bit 的数字指纹/签名.
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
                simHashBuffer.append("1");
            } else {
                simHashBuffer.append("0");
            }
        }

        return fingerprint;
    }


    private static BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            try {
                byte[] sourceArray = source.getBytes("utf-8");
                BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
                BigInteger m = new BigInteger("1000003");
                BigInteger mask = new BigInteger("2").pow(hashbits).subtract(new BigInteger("1"));
                for (byte item : sourceArray) {
                    BigInteger temp = BigInteger.valueOf((long) item);
                    x = x.multiply(m).xor(temp).and(mask);
                }
                x = x.xor(new BigInteger(String.valueOf(source.length())));
                if (x.equals(new BigInteger("-1"))) {
                    x = new BigInteger("-2");
                }
                return x;
            } catch (UnsupportedEncodingException ex) {
                return new BigInteger("0");
            }
        }
    }

    public static int hammingDistance(BigInteger intSimHash, BigInteger other) {

        BigInteger x = intSimHash.xor(other);
        int tot = 0;

        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }


}
