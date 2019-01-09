package edu.upc.citm.android.speakerfeedback;

public class RecentRoomItem {

    private String name;
    private String password;

    public RecentRoomItem() {}

    public RecentRoomItem(String name) {
        this.name = name;
    }

    public RecentRoomItem(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}