package ho.artisan.anno.mod.data;

import ho.artisan.anno.core.Registration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Reads annotations off scanned registrations and builds a platform-independent data model.
 * <p>
 * A collector must not touch Minecraft types — that is what keeps it unit-testable and is
 * why the split with {@link DataEmitter} exists. It receives every registration queued for
 * its {@link #kind()} at once, so it owns how those are merged.
 *
 * <pre>{@code
 * public final class RecipeCollector implements DataCollector<List<RecipeSpec>> {
 *     public static final DataKind<List<RecipeSpec>> KIND = DataKind.of("recipes");
 *
 *     @Override public DataKind<List<RecipeSpec>> kind() { return KIND; }
 *
 *     @Override
 *     public List<RecipeSpec> collect(String modId, List<Registration> registrations) {
 *         // read @Recipe off each entry, return specs
 *     }
 * }
 * }</pre>
 *
 * @param <D> the data model produced for the matching {@link DataEmitter}
 */
public interface DataCollector<D> {

    /** The kind this collector produces data for. */
    @NotNull
    DataKind<D> kind();

    /**
     * Collects data from all registrations queued for this kind.
     *
     * @param modId         the mod namespace
     * @param registrations the queued registration classes, in the order they were added
     * @return the collected model, empty (not {@code null}) when there is nothing to generate
     */
    @NotNull
    D collect(@NotNull String modId, @NotNull List<Registration> registrations);
}
