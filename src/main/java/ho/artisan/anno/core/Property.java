package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public final class Property extends AbstractAnno {
    private final String name;
    private final Object value;

    private Property(Object instance, Field field) throws IllegalAccessException {
        super(field);
        field.setAccessible(true);
        this.value = field.get(instance);
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

    @Override
    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Value{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    public static Property wrap(@NotNull Object instance, @NotNull Field field) {
        try {
            if (Modifier.isStatic(field.getModifiers()))
                throw new RuntimeException(field + " is static!");
            else if (Modifier.isFinal(field.getModifiers()))
                throw new RuntimeException(field + " is final!");
            return new Property(instance, field);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
