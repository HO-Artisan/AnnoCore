package ho.artisan.anno.util;

import ho.artisan.anno.core.Anno;

import java.util.Comparator;

public final class AnnoUtil {
    private AnnoUtil() {}

    public static String mergeID(Anno anno1, Anno anno2) {
        return anno1.id() + ":" + anno2.id();
    }

    public static <A extends Anno> Comparator<A> comparator() {
        return (anno1, anno2) -> anno2.priority() - anno1.priority();
    }
}
