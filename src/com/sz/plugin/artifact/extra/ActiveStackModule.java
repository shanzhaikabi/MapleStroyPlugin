package com.sz.plugin.artifact.extra;

import com.sz.plugin.artifact.ExtraModule;

public class ActiveStackModule implements ExtraModule {
    private int stack = 0;
    private boolean removeAll = false;
    private int max = 1;
    private String path = "Effect/PvPEff.img/die/PVPA4_die";
    private boolean showEffect = true;

    public ActiveStackModule(String[] args){
        for (String s:args) {
            String[] k = s.split("!");
            String type = k[0];
            switch (type) {
                case "stack":
                    stack = Integer.valueOf(k[1]);
                    break;
                case "max":
                    max = Integer.valueOf(k[1]);
                    break;
                case "remove_all":
                    removeAll = Boolean.valueOf(k[1]);
                    break;
                case "path":
                    path = k[1];
                    break;
                case "show_effect":
                    showEffect = Boolean.valueOf(k[1]);
                    break;
            }
        }
    }

    public void setReady(){
        stack++;
        if (stack > max) stack = max;
    }

    public void setReady(int i){
        stack += i;
        if (stack > max) stack = max;
    }

    public boolean isReady(){
        return this.stack > 0;
    }

    public int getStack(){
        return stack;
    }

    public int getMax(){
        return max;
    }

    public boolean isMax(){
        return stack == max;
    }

    public String getPath(){
        return path;
    }

    public boolean showEffect() {
        return showEffect;
    }

    public void removeReady(){
        if (removeAll) stack = 0;
        stack--;
        if (stack < 0) stack = 0;
    }
}
