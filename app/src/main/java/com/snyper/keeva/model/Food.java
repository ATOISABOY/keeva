package com.snyper.keeva.model;

/**
 * Created by stephen snyper on 9/5/2018.
 */

public class Food {


    private String Name,
    Image,
    Description,
    Discount,
    Price,
    MenuId;


    public Food() {
    }


    public Food(String name, String image, String description, String discount, String price, String menuId) {
        Name = name;
        Image = image;
        Description = description;
        Discount = discount;
        Price = price;
        MenuId = menuId;
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getDiscount() {
        return Discount;
    }

    public void setDiscount(String discount) {
        Discount = discount;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }
}
