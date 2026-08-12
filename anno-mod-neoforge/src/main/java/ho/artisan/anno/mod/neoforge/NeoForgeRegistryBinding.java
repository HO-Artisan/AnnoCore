package ho.artisan.anno.mod.neoforge;

import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryEntry;
import ho.artisan.anno.mod.registry.RegistryKind;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Binds a {@link RegistryKind} to a NeoForge {@link DeferredRegister}, deferring registration
 * to the mod event bus.
 * <p>
 * Two flavours: {@link #of} for a known registry and value type (listed in
 * {@link NeoForgePlatform#bindings()} and eligible for type inference), and {@link #byId} which
 * builds the {@link ResourceKey} straight from the id, covering modded and unlisted registries
 * with no code here.
 *
 * @param <T> the registry's value type
 */
public final class NeoForgeRegistryBinding<T> implements RegistryBinding<T> {
    private final RegistryKind<T> kind;
    private final Class<T> type;
    private final ResourceKey<? extends Registry<T>> registryKey;

    private NeoForgeRegistryBinding(RegistryKind<T> kind, @Nullable Class<T> type,
                                    ResourceKey<? extends Registry<T>> registryKey) {
        this.kind = kind;
        this.type = type;
        this.registryKey = registryKey;
    }

    /** Binds a known registry, with its value type exposed for inference. */
    @NotNull
    public static <T> NeoForgeRegistryBinding<T> of(@NotNull String registryId, @NotNull Class<T> type,
                                                    @NotNull ResourceKey<? extends Registry<T>> registryKey) {
        return new NeoForgeRegistryBinding<>(RegistryKind.of(registryId), type, registryKey);
    }

    /**
     * Binds a registry by id alone. The key is well-formed for any valid id, so the registry only
     * has to exist by the time the bus fires.
     *
     * @return a binding, or {@code null} if the id is not a valid {@link ResourceLocation}
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static RegistryBinding<Object> byId(@NotNull RegistryKind<?> kind) {
        ResourceLocation id = ResourceLocation.tryParse(kind.id());
        if (id == null) return null;
        ResourceKey<Registry<Object>> key = ResourceKey.createRegistryKey(id);
        return new NeoForgeRegistryBinding<>((RegistryKind<Object>) kind, null, key);
    }

    @Override
    public @NotNull RegistryKind<T> kind() {
        return kind;
    }

    @Override
    public @Nullable Class<T> type() {
        return type;
    }

    @Override
    public void register(@NotNull String modId, @NotNull List<RegistryEntry<T>> entries, Object platformContext) {
        DeferredRegister<T> deferred = DeferredRegister.create(registryKey, modId);
        for (RegistryEntry<T> entry : entries) {
            T value = entry.value();
            deferred.register(entry.id(), () -> value);
        }
        deferred.register(requireBus(platformContext));
    }

    private static IEventBus requireBus(Object platformContext) {
        if (platformContext instanceof IEventBus bus) {
            return bus;
        }
        throw new IllegalArgumentException(
                "NeoForge registration requires an IEventBus as platformContext, got: " +
                        (platformContext == null ? "null" : platformContext.getClass().getName()));
    }
}
