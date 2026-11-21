package com.fongmi.android.tv.bean;

/** Data for the Netflix-style home hero banner. */
public class Hero {

    private String name;
    private String pic;
    private String desc;

    public static Hero create() {
        return new Hero();
    }

    public String getName() {
        return name;
    }

    public Hero name(String name) {
        this.name = name;
        return this;
    }

    public String getPic() {
        return pic;
    }

    public Hero pic(String pic) {
        this.pic = pic;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public Hero desc(String desc) {
        this.desc = desc;
        return this;
    }
}
