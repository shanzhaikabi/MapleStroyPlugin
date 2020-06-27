package com.sz.plugin.status.damage;

public class NormalArea implements DamageArea{
    private double value = 1;
    private final String areaName = "普通";

    @Override
    public String getAreaName() {
        return areaName;
    }

    @Override
    public double calculateArea() {
        return value;
    }
}
