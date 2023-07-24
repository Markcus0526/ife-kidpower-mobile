package org.caloriecloud.android.model;


import java.io.Serializable;

public class DeviceSetting implements Serializable {
    private String type;
    private int height;
    private int weight;
    private int stride;
    private String message;

    public DeviceSetting(int height, String message, int stride, String type, int weight) {
        this.height = height;
        this.message = message;
        this.stride = stride;
        this.type = type;
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}
