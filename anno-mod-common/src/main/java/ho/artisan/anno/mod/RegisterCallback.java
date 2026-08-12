package ho.artisan.anno.mod;

import ho.artisan.anno.core.Entry;
import org.jetbrains.annotations.NotNull;

/**
 * Functional callback for {@link RegistrationDispatcher#on(Class, RegisterCallback)}.
 * Receives the mod context, entry metadata, and the type-cast entry value.
 *
 * @param <T> the Java type this callback handles (e.g. {@code Item.class})
 */
@FunctionalInterface
public interface RegisterCallback<T> {
    /**
     * Register a single entry.
     *
     * @param ctx   mod context (mod ID + registration)
     * @param entry the raw entry with {@code @ID}, {@code @Priority} metadata
     * @param value the entry's value cast to {@code T}
     */
    void register(@NotNull ModContext ctx, @NotNull Entry entry, @NotNull T value);
}
