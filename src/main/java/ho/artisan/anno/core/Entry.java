package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Entry extends AbstractAnno {
    private final String name;
    private final Object value;

    private Entry(Field field) throws IllegalAccessException {
        super(field);
        field.setAccessible(true);
        this.value = field.get(null);
        this.name = field.getName();
    }

    public Object raw() {
        return value;
    }

    public <T> T as(Class<T> tClass) {
        return tClass.cast(this.value);
    }

    public boolean is(Class<?> tClass) {
        return tClass.isInstance(this.value);
    }

    public boolean matches(String name) {
        return this.name.equals(name);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public static Entry wrap(@NotNull Field field) {
        try {
            if (!Modifier.isStatic(field.getModifiers()))
                throw new RuntimeException(field + " is not static!");
            return new Entry(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
