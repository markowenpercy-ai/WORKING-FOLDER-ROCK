package com.go2super.packet.raids;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class ResponseCaptureArkInfoPacket extends Packet {

    public static final int TYPE = 1454;

    private UnsignedChar place = UnsignedChar.of(0);

    private UnsignedChar roomId = UnsignedChar.of(0);
    private UnsignedChar capture = UnsignedChar.of(0);
    private UnsignedChar search = UnsignedChar.of(0);

    private UnsignedShort countdown = UnsignedShort.of(0);

    private UnsignedChar reserve = UnsignedChar.of(0);
    private UnsignedChar spareType = UnsignedChar.of(0);

    private UnsignedInteger spareTime = UnsignedInteger.of(0);

    public static void main(String... strings) {

        int val = (2 << 4) | 5;
        System.out.println(val);

        String min = StringUtils.leftPad(Integer.toBinaryString(1), 4, '0');
        String max = StringUtils.leftPad(Integer.toBinaryString(3), 4, '0');
        System.out.println("0b" + min + max);
        int value = Integer.parseInt(String.format("%08d", Long.parseLong(min + max)));
        int searchCount = (val & 240) >> 4;
        int searchNum = val & 15;
        System.out.println(searchCount);
        System.out.println(searchNum);
    }

}
