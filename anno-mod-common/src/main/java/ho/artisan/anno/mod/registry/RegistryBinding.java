package ho.artisan.anno.mod.registry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Writes entries into one platform registry. The platform-side half of a {@link RegistryKind}.
 * <p>
 * Bindings live in the platform modules, where the registry objects and Minecraft types exist.
 * Each is reached via {@link ho.artisan.anno.mod.AnnoPlatform#bindings()} (the ones exposed to
 * type inference) or {@link ho.artisan.anno.mod.AnnoPlatform#bindingFor} (resolved on demand by
 * id, so unlisted and modded registries work with no new platform code).
 * <p>
 * {@code platformContext} is the untyped runtime object from
 * {@link ho.artisan.anno.mod.AnnoMod#commit} — a NeoForge {@code IEventBus}, or {@code null} on
 * Fabric. Match it yourself and fail loudly on a mismatch.
 *
 * @param <T> the value type this registry accepts
 */
public interface RegistryBinding<T> {

    /** The kind this binding writes to. */
    @NotNull
    RegistryKind<T> kind();

    /**
     * The Java type of values this registry accepts, used to route entries that carry no
     * {@link RegisterTo}.
     *
     * @return the accepted type, or {@code null} if unknown — such a binding is never chosen by
     *         inference and only runs for entries that name its kind explicitly
     */
    @Nullable
    Class<T> type();

    /**
     * Registers every entry routed to this binding.
     * <p>
     * Called once per commit with all matching entries, already ordered by {@code @Priority}
     * (descending), so a binding can batch — which is what NeoForge's {@code DeferredRegister}
     * wants.
     *
     * @param modId           the mod namespace
     * @param entries         the entries routed here, priority-ordered
     * @param platformContext NeoForge {@code IEventBus}; {@code null} on Fabric
     */
    void register(@NotNull String modId, @NotNull List<RegistryEntry<T>> entries, Object platformContext);
}
