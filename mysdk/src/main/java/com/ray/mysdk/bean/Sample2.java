package com.ray.mysdk.bean;

import java.io.Serializable;

/**
 * @author ray
 * @date 2023/8/14 10:13
 */
public class Sample2 implements Serializable {

    private int mNum;

    public int getNum() {
        return mNum;
    }

    public void setNum(final int num) {
        mNum = num;
    }

    @Override
    public String toString() {
        return "Sample2{" +
                "mNum=" + mNum +
                '}';
    }
}
