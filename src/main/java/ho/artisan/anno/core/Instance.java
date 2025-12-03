package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public final class Instance extends AbstractAnno {
    private final String name;
    private final Object instance;
    private final List<Property> properties;
    private final List<Invoker> invokers;

    private Instance(Object instance, Class<?> clazz) {
        super(clazz);
        this.instance = instance;
        this.properties = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> Property.wrap(instance, field))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.invokers = Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(method -> Invoker.wrap(instance, method))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.name = clazz.getName();
    }

    public List<Property> properties() {
        return this.properties;
    }

    public List<Invoker> invokers() {
        return this.invokers;
    }

    public Object raw() {
        return instance;
    }

    public <T> T as(Class<T> clazz) {
        return clazz.cast(instance);
    }

    public boolean is(Class<?> clazz) {
        return clazz.isInstance(instance);
    }

    public boolean matches(String name) {
        return this.name.equals(name);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", instance=" + instance +
                ", properties=" + properties +
                '}';
    }

    public static Instance wrap(@NotNull Object instance, @NotNull Class<?> clazz) {
        if (!clazz.isInstance(instance)) {
            throw new IllegalArgumentException(
                    String.format("Instance must be of type %s, but was %s",
                            clazz.getName(), instance.getClass().getName())
            );
        }
        return new Instance(instance, clazz);
    }
}
