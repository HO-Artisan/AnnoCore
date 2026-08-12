package ho.artisan.anno.mod;

import ho.artisan.anno.core.Registration;
import ho.artisan.anno.mod.data.DataCollector;
import ho.artisan.anno.mod.data.DataKind;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

/**
 * The built-in {@link DataCollector} for {@link Lang} annotations, and the reference
 * example of the collector/emitter split.
 * <p>
 * Produces {@code languageCode → (entryId → text)}. Keys are deliberately left
 * unprefixed; each platform emitter prepends the {@code item.modid.} style prefix it
 * needs, exactly as {@link LangData} documents.
 *
 * @see ho.artisan.anno.mod.data.DataEmitter
 */
public final class LangCollector implements DataCollector<Map<String, Map<String, String>>> {

    /** Kind shared with each platform's language emitter. */
    public static final DataKind<Map<String, Map<String, String>>> KIND = DataKind.of("lang");

    /** Shared instance — the collector is stateless. */
    public static final LangCollector INSTANCE = new LangCollector();

    private LangCollector() {}

    @Override
    public @NotNull DataKind<Map<String, Map<String, String>>> kind() {
        return KIND;
    }

    @Override
    public @NotNull Map<String, Map<String, String>> collect(@NotNull String modId,
                                                             @NotNull List<Registration> registrations) {
        return LangData.collect(registrations);
    }
}
