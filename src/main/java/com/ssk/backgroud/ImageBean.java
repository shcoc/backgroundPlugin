package com.ssk.backgroud;

public class ImageBean {
    private int id;
    private String url;
    private String path;
    private int type;

    public ImageBean() {

    }

    public ImageBean(int id, String url, String path, int type) {
        this.id = id;
        this.url = url;
        this.path = path;
        this.type = type;
    }

    public ImageBean(String url, String path, int type) {
        this.url = url;
        this.path = path;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageBean imageBean = (ImageBean) o;
        if (id == imageBean.id) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + type;
        return result;
    }

    @Override
    public String toString() {
        return "ImageBean{" +
                "id=" + id +
                ", url='" + url + '\'' +
                ", path='" + path + '\'' +
                ", type=" + type +
                '}';
    }
}
