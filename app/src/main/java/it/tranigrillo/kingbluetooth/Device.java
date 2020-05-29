package it.tranigrillo.kingbluetooth;

import java.util.ArrayList;

class Device {

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Integer getState() {
        return state;
    }

    public Integer getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    private String name;
    private String address;
    private int state;
    private Integer type;

    Device(Object[] deviceData) {
        this.name = deviceData[0].toString();
        this.address = deviceData[1].toString();
        this.state = (int) deviceData[2];
        this.type = (int) deviceData[3];
    }
}
