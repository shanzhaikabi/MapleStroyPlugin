package com.sz.plugin.status.damage;

public class ElementArea implements DamageArea{
    private double value = 1;
    private final String areaName = "属性";

    @Override
    public String getAreaName() {
        return areaName;
    }

    @Override
    public double calculateArea() {
        return value;
    }
}
