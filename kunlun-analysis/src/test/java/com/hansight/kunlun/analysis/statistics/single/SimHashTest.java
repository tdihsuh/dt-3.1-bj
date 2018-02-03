package com.hansight.kunlun.analysis.statistics.single;

import com.hansight.kunlun.analysis.utils.SimHash;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;


/**
 * Created by justinwan on 14-7-8.
 */
public class SimHashTest {
    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    public static Iterator<String> list(String str) {
        ArrayList<String> list = new ArrayList<String>();
        String[] strs = str.split("[ ]+");

        list.ensureCapacity(strs.length);
        for (String s : strs)
            list.add(s);

        return list.iterator();
    }

    @Test
    public void testRegularExpression() throws Exception {
        BigInteger hash1 = SimHash.simHash(list("beijing shanghai xianggang shenzhen guangzhou taibei nanjing dalian"));
        BigInteger hash2 = SimHash.simHash(list("beijing shanghai xianggang shenzhen guangzhou taibei nanjing dalian suzhou qingdao wuxi foshan"));
        BigInteger hash3 = SimHash.simHash(list("public void setUp()"));

        System.out.println(SimHash.hammingDistance(hash1, hash2));
        System.out.println(SimHash.hammingDistance(hash1, hash3));
        System.out.println(SimHash.hammingDistance(hash2, hash3));

    }


}
