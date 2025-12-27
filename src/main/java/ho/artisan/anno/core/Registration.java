package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Modifier;
import java.util.Arrays;

public final class Registration extends AbstractAnno {
    private final String name;
    private final AnnoList<Entry> entries = new AnnoList<>();

    private Registration(Class<?> clazz) {
        super(clazz);
        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
                .filter(field -> !Modifier.isPrivate(field.getModifiers()))
                .map((Entry::wrap))
                .forEach(entries::add);
        entries.sortedByPriority();

        this.name = clazz.getName();
    }

    @Override
    public String name() {
        return name;
    }

    public AnnoList<Entry> entries() {
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
