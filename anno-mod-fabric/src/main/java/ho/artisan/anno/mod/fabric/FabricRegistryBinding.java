package ho.artisan.anno.mod.fabric;

import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryEntry;
import ho.artisan.anno.mod.registry.RegistryKind;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Binds a {@link RegistryKind} to a Yarn {@link Registry}, registering immediately as Fabric expects.
 * <p>
 * Two flavours: {@link #of} for a known registry and value type (listed in
 * {@link FabricPlatform#bindings()} and eligible for type inference), and {@link #byId} which
 * looks the registry up in {@code Registries.REGISTRIES} at commit time, covering modded and
 * unlisted registries with no code here.
 *
 * @param <T> the registry's value type
 */
public final class FabricRegistryBinding<T> implements RegistryBinding<T> {
    private final RegistryKind<T> kind;
    private final Class<T> type;
    private final Registry<T> registry;

    private FabricRegistryBinding(RegistryKind<T> kind, @Nullable Class<T> type, @Nullable Registry<T> registry) {
        this.kind = kind;
        this.type = type;
        this.registry = registry;
    }

    /** Binds a known registry, with its value type exposed for inference. */
    @NotNull
    public static <T> FabricRegistryBinding<T> of(@NotNull String registryId, @NotNull Class<T> type,
                                                  @NotNull Registry<T> registry) {
        return new FabricRegistryBinding<>(RegistryKind.of(registryId), type, registry);
    }

    /**
     * Binds a registry by id, resolved from {@code Registries.REGISTRIES} on first use.
     *
     * @return a binding, or {@code null} if no such registry exists
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public static RegistryBinding<Object> byId(@NotNull RegistryKind<?> kind) {
        Identifier id = Identifier.tryParse(kind.id());
        if (id == null) return null;
        Registry<?> found = Registries.REGISTRIES.get(id);
        if (found == null) return null;
        return new FabricRegistryBinding<>(
                (RegistryKind<Object>) kind, null, (Registry<Object>) found);
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
        if (registry == null) {
            throw new IllegalStateException("Unresolved registry for " + kind);
        }
        for (RegistryEntry<T> entry : entries) {
            Registry.register(registry, Identifier.of(modId, entry.id()), entry.value());
        }
    }
}
