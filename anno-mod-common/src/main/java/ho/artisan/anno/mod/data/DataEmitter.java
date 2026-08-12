package ho.artisan.anno.mod.data;

import org.jetbrains.annotations.NotNull;

/**
 * Writes a collected data model out through a platform's data generator.
 * <p>
 * Emitters live in the platform modules, where Minecraft types are available. Each is
 * paired with a {@link DataCollector} through a shared {@link DataKind}, and is reached
 * either via {@link ho.artisan.anno.mod.AnnoPlatform#emitters()} or by explicit
 * {@link DataGenRegistry#register} for third-party kinds.
 * <p>
 * {@code platformContext} is the same untyped runtime object described on
 * {@link ho.artisan.anno.mod.AnnoPlatform} (NeoForge {@code GatherDataEvent},
 * Fabric {@code FabricDataGenerator.Pack}). Match it yourself and fail loudly on a
 * mismatch rather than silently doing nothing.
 *
 * @param <D> the data model this emitter consumes
 */
public interface DataEmitter<D> {

    /** The kind this emitter consumes data for. */
    @NotNull
    DataKind<D> kind();

    /**
     * Writes the collected data via the platform data generator.
     *
     * @param modId           the mod namespace
     * @param data            the model produced by the matching collector
     * @param platformContext platform datagen object (NeoForge {@code GatherDataEvent} /
     *                        Fabric {@code FabricDataGenerator.Pack})
     */
    void emit(@NotNull String modId, @NotNull D data, @NotNull Object platformContext);
}
