package ho.artisan.anno.core;

import ho.artisan.anno.util.AnnoUtil;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

/**
 * Registration是一个包装静态工具类的操作单元。
 */
public final class Registration extends AbstractAnno {
    private final String name;
    private final List<Entry> entries;

    private Registration(Class<?> clazz) {
        super(clazz);
        this.entries = Stream.of(clazz.getDeclaredFields())
                .filter(field -> Modifier.isStatic(field.getModifiers()))
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

    public static Registration wrap(Class<?> clazz) {
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
