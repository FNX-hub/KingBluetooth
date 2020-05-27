package it.tranigrillo.kingbluetooth;

class Device {

    private String deviceName;
    private String status;

    Device(String name, String status) {
        this.deviceName = name;
        this.status = status;
    }

    String getDeviceName() {
        return deviceName;
    }
    String getStatus() {
        return status;
    }
    void setStatus(String status) { this.status = status; }
}
