package ho.artisan.anno.mod.architectury;

import dev.architectury.registry.registries.DeferredRegister;
import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryBindings;
import ho.artisan.anno.mod.registry.RegistryEntry;
import ho.artisan.anno.mod.registry.RegistryKind;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A {@link RegistryBinding} backed by Architectury's {@link DeferredRegister}, so one binding
 * works on every loader Architectury supports.
 * <p>
 * The point of using this over the loader-native bindings is that Architectury owns the event
 * bus itself: {@link #register} ignores {@code platformContext} entirely, so
 * {@code commit(null)} is correct on both Fabric and NeoForge — no {@code IEventBus} needed —
 * and the call can live in an Architectury {@code :common} subproject.
 * <p>
 * This class is <em>not</em> wired through {@link ho.artisan.anno.mod.AnnoPlatform} — install it
 * explicitly, which takes precedence over whatever the active platform binds:
 *
 * <pre>{@code
 * // once, before commit
 * ArchitecturyRegistryBinding.install(RegistryKinds.ITEM, Item.class);
 * ArchitecturyRegistryBinding.install(RegistryKinds.BLOCK, Block.class);
 * ArchitecturyRegistryBinding.installById(RegistryKind.of("mymod:spell"));
 *
 * AnnoMod.create(MODID).register(ModContent.class).commit(null);
 * }</pre>
 *
 * <p>Only the implementation touches Minecraft types; every signature here is loader-agnostic,
 * so calling code is identical across platforms.
 *
 * @param <T> the registry's value type
 */
public final class ArchitecturyRegistryBinding<T> implements RegistryBinding<T> {
    private final RegistryKind<T> kind;
    private final Class<T> type;

    private ArchitecturyRegistryBinding(RegistryKind<T> kind, @Nullable Class<T> type) {
        this.kind = kind;
        this.type = type;
    }

    /**
     * Creates a binding whose value type is exposed for type inference.
     *
     * @param kind the target registry
     * @param type the accepted value type, e.g. {@code Item.class} in your own namespace
     */
    @NotNull
    public static <T> ArchitecturyRegistryBinding<T> of(@NotNull RegistryKind<T> kind, @NotNull Class<T> type) {
        return new ArchitecturyRegistryBinding<>(kind, type);
    }

    /**
     * Creates a binding that declares no value type. It never wins type inference and is only
     * reached when an entry names the kind explicitly, via {@code @RegisterTo} or
     * {@code AnnoMod.register(kind, Class)} — the right shape for a modded registry.
     */
    @NotNull
    public static <T> ArchitecturyRegistryBinding<T> byId(@NotNull RegistryKind<T> kind) {
        return new ArchitecturyRegistryBinding<>(kind, null);
    }

    /** {@link #of} plus {@link RegistryBindings#register}. */
    @NotNull
    public static <T> ArchitecturyRegistryBinding<T> install(@NotNull RegistryKind<T> kind, @NotNull Class<T> type) {
        ArchitecturyRegistryBinding<T> binding = of(kind, type);
        RegistryBindings.register(binding);
        return binding;
    }

    /** {@link #byId} plus {@link RegistryBindings#register}. */
    @NotNull
    public static <T> ArchitecturyRegistryBinding<T> installById(@NotNull RegistryKind<T> kind) {
        ArchitecturyRegistryBinding<T> binding = byId(kind);
        RegistryBindings.register(binding);
        return binding;
    }

    @Override
    public @NotNull RegistryKind<T> kind() {
        return kind;
    }

    @Override
    public @Nullable Class<T> type() {
        return type;
    }

    /**
     * {@inheritDoc}
     * <p>
     * {@code platformContext} is unused — Architectury resolves the loader's registration
     * mechanism itself, so {@code null} is fine on every platform.
     */
    @Override
    public void register(@NotNull String modId, @NotNull List<RegistryEntry<T>> entries, Object platformContext) {
        ResourceLocation registryId = ResourceLocation.tryParse(kind.id());
        if (registryId == null) {
            throw new IllegalStateException("Not a valid registry id: " + kind.id());
        }
        ResourceKey<Registry<T>> key = ResourceKey.createRegistryKey(registryId);
        DeferredRegister<T> deferred = DeferredRegister.create(modId, key);
        for (RegistryEntry<T> entry : entries) {
            T value = entry.value();
            deferred.register(entry.id(), () -> value);
        }
        deferred.register();
    }
}
