package ho.artisan.anno.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Entry 是一个包装静态字段的操作单元。
 */
public final class Entry extends AbstractAnno {
    private final String name;
    private final Object value;

    private Entry(Field field) throws IllegalAccessException {
        super(field);
        field.setAccessible(true);
        this.value = field.get(null);
        this.name = field.getName();
    }

    public <T> T cast(Class<T> tClass) {
        return tClass.cast(this.value);
    }

    public <T> boolean is(Class<T> tClass) {
        return tClass.isInstance(this.value);
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }

    @Override
    public String toString() {
        return "Entry{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public static Entry wrap(Field field) {
        try {
            if (!Modifier.isStatic(field.getModifiers()))
                throw new RuntimeException(field + " is not static!");
            return new Entry(field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
