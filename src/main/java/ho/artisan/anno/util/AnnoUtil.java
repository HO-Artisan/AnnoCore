package ho.artisan.anno.util;

import ho.artisan.anno.core.Anno;
import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.handler.RegistrationHandler;

import java.util.Comparator;

public final class AnnoUtil {
    public static final String ID_SEPARATOR = ":";

    private AnnoUtil() {}

    public static void processRegistration(RegistrationHandler resolver, Registration registration) {
        resolver.onBeforeRegistration(registration);
        for (Entry entry : registration.entries()) {
            if (resolver.shouldProcess(entry))
                resolver.handle(entry, registration);
        }
        resolver.onAfterRegistration(registration);
    }

    public static String genID(Anno anno1, Anno anno2) {
        return anno1.id() + ID_SEPARATOR + anno2.id();
    }

    public static <A extends Anno> Comparator<A> comparator() {
        return (a1, a2) -> Integer.compare(a2.priority(), a1.priority());
    }
}
