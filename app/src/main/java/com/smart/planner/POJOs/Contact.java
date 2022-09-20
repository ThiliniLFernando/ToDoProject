package com.smart.planner.POJOs;

public class Contact {
    private String name ;
    private String phone ;
    private boolean isSelected;

    public Contact(){}

    public Contact(String name,String phone){
        this.name = name ;
        this.phone = phone ;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
