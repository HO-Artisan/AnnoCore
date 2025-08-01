package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

public final class Registration extends AbstractAnno {
    private final String name;
    private final List<Entry> entries;

    private Registration(Class<?> clazz) {
        super(clazz);
        this.entries = Stream.of(clazz.getDeclaredFields())
                .filter(Registration::isEligibleField)
                .map((Entry::wrap))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.name = clazz.getName();
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }

    public static Registration wrap(@NotNull Class<?> clazz) {
        return new Registration(clazz);
    }

    public static boolean isEligibleField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) && !Modifier.isFinal(modifiers);
    }

    @Override
    public String toString() {
        return "Registration{" +
                "name='" + this.name + '\'' +
                ", fields=" + entries() +
                '}';
    }
}
