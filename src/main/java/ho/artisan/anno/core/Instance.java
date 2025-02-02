package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

public final class Instance extends AbstractAnno {
    private final String name;
    private final Object instance;
    private final List<MemberEntry> memberEntries;
    private final List<Handler> handlers;

    private Instance(Object instance, Class<?> clazz) {
        super(clazz);
        this.memberEntries = Stream.of(clazz.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isFinal(field.getModifiers()))
                .map(field -> MemberEntry.wrap(instance, field))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.handlers = Stream.of(clazz.getDeclaredMethods())
                .map(method -> Handler.wrap(instance, method))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.instance = instance;
        this.name = clazz.getName();
    }

    public List<Handler> handlers() {
        return this.handlers;
    }

    public List<MemberEntry> values() {
        return this.memberEntries;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", instance=" + instance +
                ", values=" + memberEntries +
                ", handlers=" + handlers +
                '}';
    }

    public static Instance wrap(Object instance, Class<?> clazz) {
        return new Instance(instance, clazz);
    }
}
