package com.go2super.database.entity.sub;

import java.util.*;

public class CorpMembers {

    private List<CorpMember> members = new ArrayList<>();

    private List<CorpMember> recruits = new ArrayList<>();

    public List<CorpMember> getMembers() {

        return members;
    }

    public List<CorpMember> getRecruits() {

        return recruits;
    }

    public void addMember(CorpMember member) {

        if (members == null) {
            members = new ArrayList<>();
        }

        members.add(member);

    }

    public void removeMember(int guid) {

        CorpMember member = getMember(guid);
        members.remove(member);

    }

    public void addRecruit(CorpMember recruit) {

        if (recruits == null) {
            members = new ArrayList<>();
        }

        recruits.add(recruit);

    }

    public void removeRecruit(int guid) {

        CorpMember recruit = getRecruit(guid);
        recruits.remove(recruit);

    }

    public CorpMember getRecruit(int guid) {

        for (CorpMember recruit : recruits) {
            if (recruit.getGuid() == guid) {
                return recruit;
            }
        }

        return null;

    }

    public CorpMember getMember(int guid) {

        for (CorpMember member : members) {
            if (member.getGuid() == guid) {
                return member;
            }
        }

        return null;

    }

    public CorpMember getLeader() {

        for (CorpMember member : members) {
            if (member.getRank() == 1) {
                return member;
            }
        }

        return null;

    }


}
