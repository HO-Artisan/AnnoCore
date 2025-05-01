package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

public final class Instance extends AbstractAnno {
    private final String name;
    private final Object instance;
    private final List<Property> properties;

    private Instance(Object instance, Class<?> clazz) {
        super(clazz);
        this.properties = Stream.of(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .map(field -> Property.wrap(instance, field))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.instance = instance;
        this.name = clazz.getName();
    }

    public List<Property> properties() {
        return this.properties;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", instance=" + instance +
                ", properties=" + properties +
                '}';
    }

    public static Instance wrap(Object instance, Class<?> clazz) {
        return new Instance(instance, clazz);
    }
}
