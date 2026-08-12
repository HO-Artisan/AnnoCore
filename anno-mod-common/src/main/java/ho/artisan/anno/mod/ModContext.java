package ho.artisan.anno.mod;

import ho.artisan.anno.core.Registration;
import org.jetbrains.annotations.NotNull;

/**
 * Holds the mod ID and {@link Registration} for the current registration pass.
 * Created from a {@code Registration} via {@link #of(Registration)}.
 */
public record ModContext(@NotNull String modId, @NotNull Registration registration) {

    /**
     * Extracts the mod context from a registration.
     * The registration's class must be annotated with {@link AutoRegister}.
     *
     * @param registration the class-level registration wrapper
     * @return a new {@code ModContext} carrying the mod ID and registration
     * @throws ho.artisan.anno.exception.AnnotationNotFoundException
     *         if the registration's class is not annotated with {@code @AutoRegister}
     */
    @NotNull
    public static ModContext of(@NotNull Registration registration) {
        String modId = registration.get(AutoRegister.class).value();
        return new ModContext(modId, registration);
    }
}
