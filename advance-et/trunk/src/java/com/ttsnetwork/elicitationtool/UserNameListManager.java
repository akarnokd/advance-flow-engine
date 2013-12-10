package com.ttsnetwork.elicitationtool;

import java.util.ArrayList;

/**
 *
 * @author farago
 */
public class UserNameListManager {

    public static ArrayList<String> userNameList = new ArrayList<String>();
//private static UserManager instance = new UserManager();

    /*
     * public UserManager getInstance(){ return instance; }
     */
    public ArrayList<String> getUserList() {
        return userNameList;
    }

    public void addUserName(String userName) {
        userNameList.add(userName);
    }

    public void removeUserName(String userName) {
        userNameList.remove(userName);
    }

    public boolean userNameExists(String userName) {
        return userNameList.contains(userName);
    }
}
