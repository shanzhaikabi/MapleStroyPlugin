package com.sz.plugin.artifact;

public interface ActiveModule {
    boolean isActive();

    void onTrigger(double radio);
}
