package ho.artisan.anno.mod;

import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.handler.RegistrationHandler;
import ho.artisan.anno.util.PriorityLevel;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link RegistrationHandler} that filters entries by their value's Java type.
 * <p>
 * Subclasses implement {@link #register(ModContext, Entry, Object)} which receives
 * the entry value cast to {@code <T>}, eliminating the need for manual
 * {@code is() / as()} checks.
 *
 * <pre>{@code
 * // NeoForge example:
 * public class ItemHandler extends TypedRegistrationHandler<Item> {
 *     private final DeferredRegister<Item> deferred;
 *
 *     public ItemHandler(DeferredRegister<Item> d) {
 *         super(Item.class);
 *         this.deferred = d;
 *     }
 *
 *     @Override
 *     protected void register(ModContext ctx, Entry entry, Item item) {
 *         deferred.register(ctx.modId() + ":" + entry.id(), () -> item);
 *     }
 *
 *     @Override
 *     public String id() { return "neoforge_item"; }
 * }
 * }</pre>
 *
 * @param <T> the value type this handler processes
 */
public abstract class TypedRegistrationHandler<T> implements RegistrationHandler {
    private final Class<T> type;

    protected TypedRegistrationHandler(@NotNull Class<T> type) {
        this.type = type;
    }

    @Override
    public final boolean shouldProcess(@NotNull Entry entry) {
        return entry.is(type);
    }

    @Override
    public final void handle(@NotNull Entry entry, @NotNull Registration registration) {
        ModContext ctx = ModContext.of(registration);
        register(ctx, entry, entry.as(type));
    }

    /**
     * Called for each entry whose value is an instance of {@code <T>}.
     * Subclasses perform platform-specific registration here.
     *
     * @param ctx   mod context containing mod ID and registration
     * @param entry the raw entry with metadata ({@code @ID}, {@code @Priority})
     * @param value the entry's value cast to {@code T}
     */
    protected abstract void register(@NotNull ModContext ctx, @NotNull Entry entry, @NotNull T value);

    @Override
    public abstract String id();

    @Override
    public PriorityLevel priority() {
        return PriorityLevel.MEDIUM;
    }
}
