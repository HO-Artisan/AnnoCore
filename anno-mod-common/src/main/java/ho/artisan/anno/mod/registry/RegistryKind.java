package ho.artisan.anno.mod.registry;

import org.jetbrains.annotations.NotNull;

/**
 * A typed token identifying one target registry by its namespaced id
 * ({@code "minecraft:item"}, {@code "mymod:spell"}).
 * <p>
 * A kind is the contract between {@link ho.artisan.anno.mod.AnnoMod} in common code and a
 * {@link RegistryBinding} in a platform module. Items and blocks are not special: they are
 * simply the two kinds every platform binds out of the box. The type parameter {@code T} is
 * carried for compile-time safety on the platform side only — identity is decided by
 * {@link #id()} alone, which is what lets common code name a registry it cannot reference.
 * <p>
 * Use {@link RegistryKinds} for the vanilla ids, or declare your own:
 *
 * <pre>{@code
 * public static final RegistryKind<Spell> SPELLS = RegistryKind.of("mymod:spell");
 * }</pre>
 *
 * <p>Deliberately separate from {@link ho.artisan.anno.mod.data.DataKind}: that one names a
 * datagen output with a free-form id, this one names a game registry and is expected to be
 * a valid {@code namespace:path} identifier.
 *
 * @param <T> the value type this registry accepts
 */
public final class RegistryKind<T> {
    private final String id;

    private RegistryKind(@NotNull String id) {
        this.id = id;
    }

    /**
     * Creates a kind for the given registry id.
     *
     * @param id  namespaced registry id, e.g. {@code "minecraft:enchantment"}
     * @param <T> the value type this registry accepts
     */
    @NotNull
    public static <T> RegistryKind<T> of(@NotNull String id) {
        return new RegistryKind<>(id);
    }

    /** The registry id this kind is matched by. */
    @NotNull
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RegistryKind<?> other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "RegistryKind[" + id + "]";
    }
}
