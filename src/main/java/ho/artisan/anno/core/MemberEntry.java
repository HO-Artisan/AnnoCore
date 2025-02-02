package ho.artisan.anno.core;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
/**
 * Entry 是一个包装成员内字段的操作单元。
 */
public final class MemberEntry extends AbstractAnno {
    private final String name;
    private final Object value;

    private MemberEntry(Object instance, Field field) throws IllegalAccessException {
        super(field);
        field.setAccessible(true);
        this.value = field.get(instance);
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
        return "Value{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public static MemberEntry wrap(Object instance, Field field) {
        try {
            if (Modifier.isStatic(field.getModifiers()))
                throw new RuntimeException(field + " is static!");
            else if (Modifier.isFinal(field.getModifiers()))
                throw new RuntimeException(field + " is final!");
            return new MemberEntry(instance, field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
