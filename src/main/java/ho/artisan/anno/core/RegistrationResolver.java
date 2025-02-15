package ho.artisan.anno.core;

import org.jetbrains.annotations.NotNull;

public interface RegistrationResolver extends Comparable<RegistrationResolver> {
    boolean match(Entry entry);

    void process(Entry entry, Registration registration);

    String id();

    default void before(Registration registration) {}

    default void after(Registration registration) {}

    default int priority() {
        return 0;
    }

    default int compareTo(@NotNull RegistrationResolver resolver) {
        return resolver.priority() - this.priority();
    }
}
