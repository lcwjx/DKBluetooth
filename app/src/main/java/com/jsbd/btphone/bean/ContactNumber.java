package com.jsbd.btphone.bean;


import com.jsbd.bluetooth.bean.Contact;

/**
 * Created by qy128 on 2018/11/9.
 */

public class ContactNumber {
    private Contact.NumberType numberType;
    private String number;

    public ContactNumber(Contact.NumberType numberType, String number) {
        this.numberType = numberType;
        this.number = number;
    }

    public Contact.NumberType getNumberType() {
        return numberType;
    }

    public void setNumberType(Contact.NumberType numberType) {
        this.numberType = numberType;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    @Override
    public String toString() {
        return "ContactNumber{" +
                "numberType=" + numberType +
                ", number='" + number + '\'' +
                '}';
    }
}
