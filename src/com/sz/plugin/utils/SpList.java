package com.sz.plugin.utils;

import java.util.ArrayList;

public class SpList<T> extends ArrayList<T>{
    public boolean contains(Class c) {
        return get(c) != null;
    }

    public T get(Class c) {
        for(T o : this){
            if (c.isInstance(o)) return o;
        }
        return null;
    }
}
