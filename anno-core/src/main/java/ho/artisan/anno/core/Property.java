package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Property extends AbstractAnno {
    private final String name;
    private final Object instance;
    private final Field field;

    private Property(Object instance, Field field) {
        super(field);
        field.setAccessible(true);
        this.instance = instance;
        this.field = field;
        this.name = field.getName();
    }

    public Object raw() {
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T as(Class<T> tClass) {
        return tClass.cast(raw());
    }

    public boolean is(Class<?> tClass) {
        return tClass.isInstance(raw());
    }

    public void setValue(Object newValue) {
        try {
            field.set(instance, newValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean matches(String name) {
        return this.name.equals(name);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Property{" +
                "name='" + name + '\'' +
                ", value=" + raw() +
                '}';
    }

    public static Property wrap(@NotNull Object instance, @NotNull Field field) {
        if (Modifier.isStatic(field.getModifiers()))
            throw new RuntimeException(field + " is static!");
        else if (Modifier.isFinal(field.getModifiers()))
            throw new RuntimeException(field + " is final!");
        return new Property(instance, field);
    }
}
