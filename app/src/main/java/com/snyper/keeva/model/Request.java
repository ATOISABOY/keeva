package com.snyper.keeva.model;

import java.util.List;

/**
 * Created by stephen snyper on 9/8/2018.
 */

public class Request {

    private String phone;
    private String name;
    private String  address;
    private String  total;
    private String status="0";
    private List<Order> foods;
    private String comment;
    private String paymentMethod;
    private String paymentState;
    private String latLng;

    public Request() {

      //  this.status="0";
    }


    public Request(String phone, String name, String address, String total, String status, List<Order> foods, String comment, String paymentMethod, String paymentState, String latLng) {
        this.phone = phone;
        this.name = name;
        this.address = address;
        this.total = total;
        this.status = status;
        this.foods = foods;
        this.comment = comment;
        this.paymentMethod = paymentMethod;
        this.paymentState = paymentState;
        this.latLng = latLng;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal() {
        return total;
    }

    public void setTotal(String total) {
        this.total = total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Order> getFoods() {
        return foods;
    }

    public void setFoods(List<Order> foods) {
        this.foods = foods;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getLatLng() {
        return latLng;
    }

    public void setLatLng(String latLng) {
        this.latLng = latLng;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentState() {
        return paymentState;
    }

    public void setPaymentState(String paymentState) {
        this.paymentState = paymentState;
    }
}
