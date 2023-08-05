package net.dbinfun.ioc.beans;

public class BeanInfo{
    private final String name;
    private final BeanType type;

    private final Class<?> cls;
    private final Object object;

    public BeanInfo(String name, BeanType type, Class<?> cls, Object object) {
        this.name = name;
        this.type = type;
        this.object = object;
        this.cls = cls;
    }

    public String getName() {
        return name;
    }

    public BeanType getType() {
        return type;
    }

    public Object getObject() {
        return object;
    }

    public Class<?> getCls() {
        return cls;
    }
}