package ho.artisan.anno.util;

import ho.artisan.anno.core.Anno;
import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.RegistrationResolver;

import java.util.Comparator;

public final class AnnoUtil {
    private AnnoUtil() {}

    public static void process(RegistrationResolver resolver, Registration registration) {
        resolver.before(registration);
        for (Entry entry : registration.entries()) {
            if (resolver.match(entry))
                resolver.process(entry, registration);
        }
        resolver.after(registration);
    }

    public static String genID(Anno anno1, Anno anno2) {
        return anno1.id() + ":" + anno2.id();
    }

    public static <A extends Anno> Comparator<A> comparator() {
        return (anno1, anno2) -> anno2.priority() - anno1.priority();
    }
}
