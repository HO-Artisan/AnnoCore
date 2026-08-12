package ho.artisan.anno.mod.data;

import org.jetbrains.annotations.NotNull;

/**
 * A typed token identifying one category of generated data (lang files, recipes, tags, ...).
 * <p>
 * A kind is the contract between a platform-independent {@link DataCollector} and a
 * platform-specific {@link DataEmitter}: both declare the same kind, and
 * {@link DataGenRegistry} pairs them at generation time. The type parameter {@code D}
 * is the data model handed from the collector to the emitter; it is carried for
 * compile-time safety only, since identity is decided by {@link #id()} alone.
 * <p>
 * Declare a kind once, in a place both sides can see:
 *
 * <pre>{@code
 * public static final DataKind<List<RecipeSpec>> RECIPES = DataKind.of("recipes");
 * }</pre>
 *
 * @param <D> the collected data model for this kind
 */
public final class DataKind<D> {
    private final String id;

    private DataKind(@NotNull String id) {
        this.id = id;
    }

    /**
     * Creates a kind with the given identifier.
     * <p>
     * Two kinds with equal ids are equal regardless of {@code D}, which is what lets a
     * collector in common code and an emitter in a platform module find each other.
     *
     * @param id  unique identifier, e.g. {@code "lang"}
     * @param <D> the collected data model
     */
    @NotNull
    public static <D> DataKind<D> of(@NotNull String id) {
        return new DataKind<>(id);
    }

    /** The identifier this kind is matched by. */
    @NotNull
    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof DataKind<?> other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "DataKind[" + id + "]";
    }
}
