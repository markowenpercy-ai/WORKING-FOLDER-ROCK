package com.go2super.database.entity.sub;

import com.go2super.obj.type.MailType;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.fight.ResponseFightResultPacket2;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class Email implements Comparable<Email> {

    private int autoId;

    // 0 = Private Message
    // 1 = Battle Mail
    // 2 = ?
    // 3 = Ships
    // 4 = Resources
    // 5 = Player Email
    // 6 = ?
    private int type;
    private int readFlag;

    private String subject;
    private String emailContent;

    private int fightGalaxyId = -1;

    private String name;

    private int guid;
    private Date date;

    private List<EmailGood> goods = new ArrayList<>();
    private MailType mailType;
    private ResponseFightResultPacket2 fightResultPacket;

    public boolean hasGoods() {

        if (goods == null) {
            return false;
        }
        return !goods.isEmpty();
    }

    public void addGood(int propId, int lockNum) {

        goods.add(EmailGood.builder()
            .goodId(propId)
            .lockNum(lockNum)
            .build());
    }

    public void addGood(EmailGood emailGood) {

        goods.add(emailGood);
    }

    public EmailGood getEmailGood(int goodId) {

        for (EmailGood emailGood : goods) {
            if (emailGood.getGoodId() == goodId) {
                return emailGood;
            }
        }
        return null;
    }

    @Override
    public int compareTo(Email other) {

        if (date.after(other.getDate())) {
            return -1;
        }
        return 1;
    }

}
