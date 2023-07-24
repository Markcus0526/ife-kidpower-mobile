package org.caloriecloud.android.model;

import java.io.Serializable;

public class Organization implements Serializable{
    private int _id;
    private String name;
    private String imageSrc;
    private String createdAt;
    private String updatedAt;

    public Organization(int _id, String createdAt, String imageSrc, String name, String updatedAt) {
        this._id = _id;
        this.createdAt = createdAt;
        this.imageSrc = imageSrc;
        this.name = name;
        this.updatedAt = updatedAt;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
