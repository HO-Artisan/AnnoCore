package ho.artisan.anno.core.handler;

import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import org.jetbrains.annotations.NotNull;

public interface RegistrationHandler extends Comparable<RegistrationHandler> {
    boolean match(Entry entry);

    void process(Entry entry, Registration registration);

    String id();

    default void before(Registration registration) {}

    default void after(Registration registration) {}

    default int priority() {
        return 0;
    }

    default int compareTo(@NotNull RegistrationHandler resolver) {
        return resolver.priority() - this.priority();
    }
}
