package com.go2super.database.entity.sub;

import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserEmailStorage {

    private List<Email> userEmails = new ArrayList<>();

    public List<Email> getUserEmails() {

        return userEmails;
    }

    public void addEmail(Email email) {

        if (userEmails == null) {
            userEmails = new ArrayList<>();
        }

        userEmails.add(email);

    }

    public Email getEmail(int id) {

        /*List<Email> emails = getSortedEmails();

        if(emails.size() <= id) return null;
        return emails.get(id);*/
        for (Email email : getSortedEmails()) {
            if (email.getAutoId() == id) {
                return email;
            }
        }

        return null;

    }

    public int nextAutoId() {

        return nextAutoId(0);
    }

    private int nextAutoId(int autoId) {

        for (Email email : getSortedEmails()) {
            if (email.getAutoId() == autoId) {
                return nextAutoId(++autoId);
            }
        }
        return autoId;
    }

    public int getAutoId(Email email) {

        return getSortedEmails().indexOf(email);
    }

    public List<Email> getSortedEmails() {

        List<Email> emails = new ArrayList<>(userEmails);
        Collections.sort(emails);
        return emails;

    }

}
