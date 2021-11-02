/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-29 09:08:31
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-30 15:53:02
 */
import java.io.*;

class test {
    public static void main(String[] args) {
        try {
            String filePath = "./resource/WizTree.exe";
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");
            Byte a = (byte)1;
            byte[] readBuf = new byte[0x28];
            raf.seek(0x1F8);
            raf.read(readBuf);

            System.out.print("byte[] sampleHeader = {");
            for (int i = 0; i < readBuf.length; i++) {
                if (i == readBuf.length - 1) {
                    System.out.printf("(byte)0x%x", readBuf[i]);
                    break;
                }
                System.out.printf("(byte)0x%x, ", readBuf[i]);
            }
            System.out.print("};");

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return;
    } 
}

class TA {
    int a;
    int b;
    TA(int p1, int p2) {
        a = p1;
        b = p2;
    }

    int f1() {
        return 1;
    }
}

class TB extends TA{
    int c;
    TB(int p1, int p2, int p3) {
        super(p1, p2);
        c = p3;
    }

    Boolean f1(Boolean input) {
        return !input;
    }
}
/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-29 09:08:31
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-29 09:08:31
 */
