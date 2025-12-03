package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

public final class Registration extends AbstractAnno {
    private final String name;
    private final List<Entry> entries;

    private Registration(Class<?> clazz) {
        super(clazz);
        this.entries = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isPrivate(field.getModifiers()))
                .map((Entry::wrap))
                .sorted(AnnoUtil.comparator())
                .toList();
        this.name = clazz.getName();
    }

    public List<Entry> entries() {
        return this.entries;
    }

    public boolean matches(String name) {
        return this.name.equals(name);
    }

    public static Registration wrap(@NotNull Class<?> clazz) {
        return new Registration(clazz);
    }

    @Override
    public String toString() {
        return "Registration{" +
                "name='" + this.name + '\'' +
                ", fields=" + entries() +
                '}';
    }
}
