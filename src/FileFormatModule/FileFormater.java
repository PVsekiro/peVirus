/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-22 01:09:38
 * @LastEditors: Yhank
 * @LastEditTime: 2021-11-01 22:16:06
 */
package FileFormatModule;

import java.io.*;
import UseFunction.UF;
import java.util.ArrayList;

public class FileFormater {

    private UF uf = new UF();

    public String desPath                      = null;
    public long peHeaderOffset                 = -1;                  // peHeader偏移
    public long fileHeaderOffset               = -1;                  // fileHeader即pe头第二个结构体的偏移
    public long optionHeaderSize               = -1;                  // 可选文件头大小
    public long optionHeaderOffset             = -1;                  // optionHeader偏移
    public long addressOfEntryPointRVA         = -1;                  // 即程序执行入口 RVA注意是RVA
    public long addressOfEntryPointRaw         = -1;                  // 程序执行入口的 文件偏移
    public long aOEPRawStore                   = -1;                  // addressOfEntryPoint在文件中的存放位置
    public long aOEPSizeOfImage                = -1;                  // 存放sizeOfImage的位置
    public long sizeOfImage                    = -1;                  // 理解应该是文件在内存中所占用的大小
    public long imageBase                      = -1;
    public long sectionAlignment               = -1;                  // 内存中的区块对齐粒度
    public long fileAlignment                  = -1;                  // 文件区块对齐粒度
    public long sectionHeadersOffset           = -1;                  // 节区表起始偏移
    public long sectionHeaderSize              = -1;
    public long sectionNum                     = -1;                  // 节的数量
    public ArrayList<SectionData> sectionDatas = new ArrayList<>();
    public ArrayList<HoleData> holes           = new ArrayList<> ();

    public static void main(String[] args) {
        FileFormater ff =new FileFormater("./resource/kernel32.dll");
        System.out.printf("%x", ff.rvaToRawpoint(0x9a1e0));
        return;
    }

    public FileFormater(String filePath) {
        desPath = filePath;
        readNeedFileData(new File(filePath));
    }

    public FileFormater(File inputFile) {
        desPath = inputFile.getPath();
        readNeedFileData(inputFile);
    }


    public long rvaToRawpoint(long rvaAddress) {
        // 将一个rva地址转成文件偏移
        long ret = -1;   //
        for (SectionData i: sectionDatas) {

            long start = i.rvaAddress;
            long phisicalEnd = start + i.phsicalSize;
            // 获得rvaEnd
            long end = ((long)Math.ceil((double)phisicalEnd / (double)sectionAlignment)) * sectionAlignment;
            if (rvaAddress >= start && rvaAddress < end) {
                long offset = rvaAddress - start;
                ret = i.pointToRawData + offset;
                break;
            }
        }
        return ret;
    }

    public long rawPointToRva(long rawPoint) {
        // 将一个文件偏移转为rva地址
        long ret = -1;   //
        for (SectionData i: sectionDatas) {
            long start = i.pointToRawData;
            long end = start + i.rawSize;
            if (rawPoint >= start && rawPoint < end) {
                long offset = rawPoint - start;
                ret = i.rvaAddress + offset;
                break;
            }
        }
        return ret;
    }

    private void readNeedFileData(File inputFile) {
        try {
            // 某些重要节点的偏移
            RandomAccessFile raf = new RandomAccessFile(inputFile, "r");
            // 获得peHeader的偏移
            byte[] readBuf0 = new byte[4];
            raf.seek(0x3c);
            raf.read(readBuf0);
            peHeaderOffset = uf.trans4Byte(readBuf0); 

            // 获得fileHeaderOffset
            fileHeaderOffset = peHeaderOffset + 4;

            // 获得sectionNum
            byte[] readBuf1 = new byte[2];
            raf.seek(fileHeaderOffset + 0x2);
            raf.read(readBuf1);
            sectionNum = uf.trans2Byte(readBuf1);

            //  获得optionHeaderSize 
            byte[] readBuf2 = new byte[2];
            raf.seek(fileHeaderOffset + 0x10);
            raf.read(readBuf2);
            optionHeaderSize = uf.trans2Byte(readBuf2);

            
            // 获得optionHeaderOffset 
            optionHeaderOffset  = peHeaderOffset + 0x18;   // optionHeader偏移

            // 获得addressOfEntryPoint
            byte[] readBuf3 = new byte[4];
            aOEPRawStore = optionHeaderOffset + 0x10;
            raf.seek(aOEPRawStore);
            raf.read(readBuf3);
            addressOfEntryPointRVA = uf.trans4Byte(readBuf3);
            
            // 获得imagebase
            byte[] readBuf4 = new byte[4];
            raf.seek(optionHeaderOffset + 0x1c);
            raf.read(readBuf4);
            imageBase = uf.trans4Byte(readBuf4);
            // readBuf4中存的就是imageBase注意大端存储

            // 获得内存对齐粒度
            byte[] readBuf5 = new byte[4];
            raf.seek(optionHeaderOffset + 0x20);
            raf.read(readBuf5);
            sectionAlignment = uf.trans4Byte(readBuf5);

            // 获得文件对齐粒度
            byte[] readBuf6 = new byte[4];
            raf.seek(optionHeaderOffset + 0x24);
            raf.read(readBuf6);
            fileAlignment = uf.trans4Byte(readBuf6);

            // 获得节区表头偏移
            sectionHeadersOffset = optionHeaderOffset + optionHeaderSize;

            // 获得各节区表项的大小
            sectionHeaderSize = 0x28;   // 看编辑器知每个都是 0x28字节

            // 读取节区表信息
            long fp = sectionHeadersOffset;
            byte[] readBuf7 = new byte[4];
            
            while(true) {
                raf.seek(fp);
                raf.read(readBuf7);
                if (uf.trans4Byte(readBuf7) == 0) break;    // 检测是否已经读完了区块表

                // 获得文件名
                byte[] readBuf8 = new byte[8];
                raf.seek(fp);
                raf.read(readBuf8);
                String param0 = new String(readBuf8);

                long param1 = fp;   // 节表头偏移

                // 获得实际大小
                raf.seek(fp + 0x8);
                raf.read(readBuf7);
                long param2 = uf.trans4Byte(readBuf7);

                // 获得考虑文件粒度的大小
                raf.seek(fp + 0x10);
                raf.read(readBuf7);
                long param3 = uf.trans4Byte(readBuf7);

                // 获得rva位置
                raf.seek(fp + 0x0C);
                raf.read(readBuf7);
                long param4 = uf.trans4Byte(readBuf7);


                // 获得文件起始偏移
                raf.seek(fp + 0x14);
                raf.read(readBuf7);
                long param5 = uf.trans4Byte(readBuf7);

                // 将数据加入ArrayList
                sectionDatas.add(new SectionData(param0, param1, param2, param3, param4, param5));


                // 矫正指针位
                fp += sectionHeaderSize;

            }

            // 通过上面获得的节数据获得程序入口的文件地址偏移
            addressOfEntryPointRaw = rvaToRawpoint(addressOfEntryPointRVA);

            // 获得空穴信息
            for (SectionData i: sectionDatas) {
                long holeRawStart = i.getPointToRawData() + i.getPhisicalSize() + 8;     // 后面的加8是为了保险
                long holeSize = i.getRawSize() - i.getPhisicalSize() - 8;
                long holeStartRva = rawPointToRva(holeRawStart);
                holes.add(new HoleData(holeRawStart, holeSize, holeStartRva));
            }

            // 获得sizeOfImage
            aOEPSizeOfImage = peHeaderOffset + 0x50;
            byte[] readBuf8 = new byte[4];
            raf.seek(aOEPSizeOfImage);
            raf.read(readBuf8);
            sizeOfImage = uf.trans4Byte(readBuf8);
            
            // 关闭文件
            raf.close();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
    }

}


