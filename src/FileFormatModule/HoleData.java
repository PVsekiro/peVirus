/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-27 18:44:13
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-27 19:04:33
 */
package FileFormatModule;

public class HoleData {
    public long holeStartRawOffset = -1;
    public long holeSize = -1;
    public long holeStartRva = -1;
    public HoleData(long holeStartRawOffset, long holeSize, long holeStartRva) {
        this.holeStartRawOffset = holeStartRawOffset;
        this.holeSize = holeSize;
        this.holeStartRva = holeStartRva;
}
    
}
/*
 * @Descripttion: your project
 * @version: 1.0
 * @Author: Yhank
 * @Date: 2021-10-27 18:44:13
 * @LastEditors: Yhank
 * @LastEditTime: 2021-10-27 18:44:14
 */
