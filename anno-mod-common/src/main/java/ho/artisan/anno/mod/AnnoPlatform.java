package ho.artisan.anno.mod;

import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.data.DataGenRegistry;
import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryBindings;
import ho.artisan.anno.mod.registry.RegistryKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Platform abstraction implemented by {@code anno-mod-fabric} and {@code anno-mod-neoforge}.
 * <p>
 * Discovered at runtime via {@link Platforms#get()} ({@link java.util.ServiceLoader}).
 * Implementations translate AnnoCore registrations into platform-native registry calls,
 * keeping all Minecraft-specific types out of the common module.
 * <p>
 * The {@code platformContext} parameters carry platform-native runtime objects that
 * cannot be abstracted away:
 * <ul>
 *   <li>registration (see {@link RegistryBinding}): NeoForge {@code IEventBus}
 *       (may be {@code null} on Fabric)</li>
 *   <li>datagen (see {@link DataEmitter}): NeoForge {@code GatherDataEvent} or
 *       Fabric {@code FabricDataGenerator.Pack}</li>
 * </ul>
 * <p>
 * Both halves are open-ended by design: this interface supplies <em>bindings</em> and
 * <em>emitters</em>, and never gains a method per registry or per data type.
 */
public interface AnnoPlatform {

    /** Platform identifier, e.g. {@code "fabric"} or {@code "neoforge"}. */
    @NotNull
    String id();

    /**
     * The registry bindings this platform provides out of the box, e.g. items and blocks.
     * <p>
     * Indexed by {@link RegistryBinding#kind()} on first lookup through {@link RegistryBindings},
     * and — for those declaring a {@link RegistryBinding#type()} — used to route entries that
     * carry no {@link ho.artisan.anno.mod.registry.RegisterTo}. Registration is deliberately
     * <em>not</em> a per-type method on this interface: a new registry means a new binding, never
     * a change here.
     *
     * @return built-in bindings; empty by default
     */
    @NotNull
    default List<RegistryBinding<?>> bindings() {
        return List.of();
    }

    /**
     * Builds a binding for a registry this platform does not list, resolved by id at runtime.
     * <p>
     * This is what lets a modded or unlisted vanilla registry work with no new platform code.
     * Such bindings are only reachable when an entry names the kind explicitly (via
     * {@code @RegisterTo} or {@link AnnoMod#register(RegistryKind, Class)}), never by type
     * inference, since the accepted type is unknown.
     *
     * @param kind the requested registry kind
     * @return a binding, or {@code null} if this platform cannot resolve the id
     */
    @Nullable
    default RegistryBinding<?> bindingFor(@NotNull RegistryKind<?> kind) {
        return null;
    }

    /**
     * The data emitters this platform provides out of the box, e.g. its language file writer.
     * <p>
     * Returned emitters are indexed by {@link DataEmitter#kind()} on first lookup through
     * {@link DataGenRegistry}. Data generation is deliberately <em>not</em> a method on this
     * interface: a new kind of generated data means a new
     * {@link ho.artisan.anno.mod.data.DataCollector} plus emitter, never a change here.
     *
     * @return built-in emitters; empty by default
     */
    @NotNull
    default List<DataEmitter<?>> emitters() {
        return List.of();
    }
}
