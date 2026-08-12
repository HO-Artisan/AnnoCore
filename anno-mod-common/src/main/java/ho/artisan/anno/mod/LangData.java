package ho.artisan.anno.mod;

import ho.artisan.anno.core.Entry;
import ho.artisan.anno.core.Registration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Collects {@link Lang} translations from an {@link AutoRegister}-annotated class.
 * <p>
 * Returns translations grouped by language code, keyed by each entry's
 * {@code @ID} value. Callers prepend type / mod prefixes when writing
 * Minecraft language JSON files.
 *
 * <pre>{@code
 * Map<String, Map<String, String>> data = LangData.collect(Registration.wrap(ModItems.class));
 * // → {"en_us": {"sword": "Iron Sword"}, "zh_cn": {"sword": "铁剑"}}
 * }</pre>
 */
public final class LangData {
    private LangData() {}

    /**
     * Collects translations from a registration.
     *
     * @param registration the scanned registration
     * @return outer map: language code → (entryId → translation text)
     */
    @NotNull
    public static Map<String, Map<String, String>> collect(@NotNull Registration registration) {
        Map<String, Map<String, String>> result = new LinkedHashMap<>();
        for (Entry entry : registration.entries().getAll()) {
            String key = entry.id();
            for (Lang lang : extractLangs(entry)) {
                result.computeIfAbsent(lang.code(), k -> new LinkedHashMap<>())
                        .put(key, lang.text());
            }
        }
        return result;
    }

    /**
     * Collects and merges translations from several registrations.
     * <p>
     * Later registrations win on a duplicate {@code (code, entryId)} pair.
     *
     * @param registrations the scanned registrations, in declaration order
     * @return outer map: language code → (entryId → translation text)
     */
    @NotNull
    public static Map<String, Map<String, String>> collect(@NotNull List<Registration> registrations) {
        Map<String, Map<String, String>> merged = new LinkedHashMap<>();
        for (Registration registration : registrations) {
            collect(registration).forEach((code, entries) ->
                    merged.computeIfAbsent(code, k -> new LinkedHashMap<>()).putAll(entries));
        }
        return merged;
    }

    @NotNull
    private static List<Lang> extractLangs(@NotNull Entry entry) {
        List<Lang> langs = new ArrayList<>();
        if (entry.contain(Lang.Container.class)) {
            Collections.addAll(langs, entry.get(Lang.Container.class).value());
        }
        if (entry.contain(Lang.class)) {
            langs.add(entry.get(Lang.class));
        }
        return langs;
    }
}
