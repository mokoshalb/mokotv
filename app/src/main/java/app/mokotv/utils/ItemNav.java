package app.mokotv.utils;

public class ItemNav {
    private Integer iconRes;
    private String name;
    private int id;

    public ItemNav(int id, Integer iconRes, String name) {
        this.id = id;
        this.iconRes = iconRes;
        this.name = name;
    }

    public Integer getIconRes() {
        return iconRes;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}