package com.hansight.kunlun.analysis.realtime.single;


/**
 * Created by justinwan on 4/16/14.
 */
public class StringEntropyCalculator {
    public static double calculate(String str) {

        int alphabet = 0;
        boolean lower = false, upper = false, numbers = false, symbols1 = false, symbols2 = false;
        String other = "";

        for(int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);

            if(!lower && "abcdefghijklmnopqrstuvwxyz".indexOf(c) >= 0) {
                alphabet += 15;
                lower = true;
            } else if(!upper && "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(c) >= 0) {
                alphabet += 15;
                upper = true;
            } else if(!numbers && "0123456789".indexOf(c) >= 0) {
                alphabet += 10;
                numbers = true;
            }
            else if(!symbols1 && "!@#$%^&*()".indexOf(c) >= 0) {
                alphabet += 10;
                symbols1 = true;
            } else if(!symbols2 && "~`-_=+[]{}\\|;:\'\",.<>?/".indexOf(c) >= 0) {
                alphabet += 22;
                symbols2 = true;
            } else if(other.indexOf(c) == -1) {
                alphabet += 1;
                other = other + c;
            }
        }

        return str.length() * Math.log(alphabet) / Math.log(2);
    }

    public static void main(String[] args) {
        System.out.println("args = " + calculate("-1'%20OR%202+731-731-1%3D0+0+0+1%20--"));
        System.out.println("args = " + calculate("23724u239424@%$*^^%**&(*^&^%^%$^*%^"));
    }
}
