package ho.artisan.anno.core.handler;

import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.util.PriorityLevel;
import org.jetbrains.annotations.NotNull;

import static ho.artisan.anno.util.PriorityLevel.LOW;

public interface RegistrationHandler extends Comparable<RegistrationHandler> {
    boolean shouldProcess(Entry entry);

    void handle(Entry entry, Registration registration);

    String id();

    default void onBeforeRegistration(Registration registration) {}

    default void onAfterRegistration(Registration registration) {}

    default PriorityLevel priority() {
        return LOW;
    }

    default int compareTo(@NotNull RegistrationHandler resolver) {
        int thisValue = this.priority().value();
        int otherValue = resolver.priority().value();
        return Integer.compare(thisValue, otherValue);
    }

}
