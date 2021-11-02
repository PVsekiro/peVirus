package FileFormatModule;

public class SectionData {
    
    String name = null;
    long headerOffset   = 0;  // 节头起始偏移
    long phsicalSize    = 0;  // 实际大小
    long rawSize        = 0;  // 文件粒度对齐后的大小
    long rvaAddress     = 0;  // 节的rva起始偏移
    long pointToRawData = 0;  // 节的文件中起始偏移

    SectionData(String name, long headerOffset, long phsicalSize, long rawSize, long rvaAddress, long pointToRawData) {

        this.name = name;
        this.headerOffset = headerOffset;
        this.phsicalSize = phsicalSize;
        this.rawSize = rawSize;
        this.rvaAddress = rvaAddress;
        this.pointToRawData = pointToRawData;

    }

    public String getName() {
        return this.name;
    }

    public long getHeaderOffset() {
        return this.headerOffset;
    }

    public long getPhisicalSize() {
        return this.phsicalSize;
    }

    public long getRawSize() {
        return this.rawSize;
    }

    public long getRvaAddress() {
        return this.rvaAddress;
    }

    public long getPointToRawData() {
        return this.pointToRawData;
    }

}
/*
/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-22 15:20:06
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-22 15:20:07
 */
