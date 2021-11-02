/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-21 09:10:38
 * @LastEditors: Yhank
 * @LastEditTime: 2021-11-01 22:05:10
 */
package UseFunction;

public class UF {

    private long transByte(byte[] inputList, int n) {

        if (inputList.length != n) return -1;
        long ret = 0;
        int bitMove = 0;
        for (byte i: inputList) {
            int k = i &  0xFF;
            ret += k << bitMove;
            bitMove += 8;
        }

        return ret;

    }

    public long trans4Byte(byte[] inputList) {
        return transByte(inputList, 4);
    }    

    public long trans2Byte(byte[] inputList) {

        return transByte(inputList, 2);

    }

    private byte[] backByte(long input, int n) {
        byte[] ret = new byte[n];
        for (int i = 0; i < n; i++) {
            ret[i] = (byte)(input & 0xFF);
            input = input >> 8;
        }
        return ret;
    }

    public byte[] back2Byte(long input) {
        return backByte(input, 2);
    }

    public byte[] back4Byte(long input) {
        return backByte(input, 4);
    }

    public long simpleCeil(long param1, long param2) {
        return (long)(Math.ceil((double)param1 / (double)param2)) * param2;
    }

}
/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-21 09:10:38
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-21 09:10:39
 */
