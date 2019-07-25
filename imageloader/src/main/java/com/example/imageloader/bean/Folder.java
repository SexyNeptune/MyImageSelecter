package com.example.imageloader.bean;

public class Folder {

    private String dir;
    private String firstImagePath;
    private String name;
    private int count;

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
        //得到目录路径便可知道名字
        int lastIndexOf = this.dir.lastIndexOf("/");
        this.name = this.dir.substring(lastIndexOf + 1);
    }

    public String getFirstImagePath() {
        return firstImagePath;
    }

    public void setFirstImagePath(String firstImagePath) {
        this.firstImagePath = firstImagePath;
    }

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "Folder{" +
                "dir='" + dir + '\'' +
                ", firstImagePath='" + firstImagePath + '\'' +
                ", name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}
