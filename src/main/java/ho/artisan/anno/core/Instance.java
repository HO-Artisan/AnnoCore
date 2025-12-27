package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class Instance extends AbstractAnno {
    private final String name;
    private final Object instance;

    private final AnnoList<Property> properties = new AnnoList<>();
    private final AnnoList<Invoker> invokers = new AnnoList<>();

    private Instance(Object instance, Class<?> clazz) {
        super(clazz);
        this.instance = instance;

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .map(field -> Property.wrap(instance, field))
                .forEach(properties::add);
        properties.sortedByPriority();

        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> !Modifier.isStatic(method.getModifiers()))
                .map(method -> Invoker.wrap(instance, method))
                .forEach(invokers::add);
        invokers.sortedByPriority();

        this.name = clazz.getName();
    }

    public AnnoList<Property> properties() {
        return this.properties;
    }

    public AnnoList<Invoker> invokers() {
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

    @Override
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
