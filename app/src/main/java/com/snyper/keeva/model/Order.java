package com.snyper.keeva.model;

/**
 * Created by stephen snyper on 9/7/2018.
 */

public class Order {

    private String UserPhone;
    private String ProductId;
    private String  ProductName;
    private String   Quality;
    private String   Price;
    private String   Discount;
    private String  Image;



    public Order() {
    }

    public Order(String userPhone, String productId, String productName, String quality, String price, String discount, String image) {
        UserPhone = userPhone;
        ProductId = productId;
        ProductName = productName;
        Quality = quality;
        Price = price;
        Discount = discount;
        Image = image;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getProductId() {
        return ProductId;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public String getQuality() {
        return Quality;
    }

    public void setQuality(String quality) {
        Quality = quality;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }
}
