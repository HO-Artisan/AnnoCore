package ho.artisan.anno.mod;

import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.handler.RegistrationHandler;
import ho.artisan.anno.util.AnnoUtil;
import ho.artisan.anno.util.PriorityLevel;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Fluent API for dispatching a {@link Registration} to type-based handlers.
 * <p>
 * Wraps the boilerplate of creating {@link TypedRegistrationHandler} instances
 * and calling {@link AnnoUtil#processRegistration} for each:
 *
 * <pre>{@code
 * RegistrationDispatcher.of(MyItems.class)
 *     .on(Item.class, (ctx, entry, item) ->
 *         itemDeferred.register(ctx.modId() + ":" + entry.id(), () -> item))
 *     .on(Block.class, (ctx, entry, block) ->
 *         blockDeferred.register(ctx.modId() + ":" + entry.id(), () -> block))
 *     .dispatch();
 * }</pre>
 */
public final class RegistrationDispatcher {
    private final Registration registration;
    private final List<RegistrationHandler> handlers = new ArrayList<>();

    private RegistrationDispatcher(@NotNull Class<?> clazz) {
        this.registration = Registration.wrap(clazz);
    }

    /**
     * Creates a dispatcher for the given registration class.
     * The class must be annotated with {@link AutoRegister}.
     */
    @NotNull
    public static RegistrationDispatcher of(@NotNull Class<?> clazz) {
        return new RegistrationDispatcher(clazz);
    }

    /**
     * Registers a handler for entries whose value is an instance of {@code type}.
     *
     * @param type     the Java type to match (e.g. {@code Item.class})
     * @param callback called for each matching entry
     * @param <T>      the value type
     * @return this dispatcher, for chaining
     */
    public <T> RegistrationDispatcher on(@NotNull Class<T> type, @NotNull RegisterCallback<T> callback) {
        handlers.add(new TypedRegistrationHandler<>(type) {
            @Override
            protected void register(@NotNull ModContext ctx, @NotNull Entry entry, @NotNull T value) {
                callback.register(ctx, entry, value);
            }

            @Override
            public String id() {
                return "dispatch:" + type.getSimpleName().toLowerCase();
            }

            @Override
            public PriorityLevel priority() {
                return PriorityLevel.MEDIUM;
            }
        });
        return this;
    }

    /**
     * Runs all registered handlers against the registration.
     * Handlers are executed in the order they were registered via {@link #on}.
     */
    public void dispatch() {
        for (RegistrationHandler handler : handlers) {
            AnnoUtil.processRegistration(handler, registration);
        }
    }
}
