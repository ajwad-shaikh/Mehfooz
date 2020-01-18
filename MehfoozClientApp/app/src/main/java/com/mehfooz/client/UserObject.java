package com.mehfooz.client;

public class UserObject {

    private String name, phoneNumber;

    public UserObject() {
        this.name = null;
        this.phoneNumber = null;
    }

    public UserObject(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isComplete() {
        if( phoneNumber != null && name != null)
            return !phoneNumber.isEmpty() && !name.isEmpty();
        else return false;
    }



    @Override
    public String toString() {
        return "UserObject{" +
                "name='" + name + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                '}';
    }
}
