package ho.artisan.anno.mod.neoforge;

import ho.artisan.anno.mod.LangCollector;
import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.data.DataKind;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Writes {@link LangCollector} output to NeoForge language files, one
 * {@link LanguageProvider} per language code.
 */
public final class NeoForgeLangEmitter implements DataEmitter<Map<String, Map<String, String>>> {

    @Override
    public @NotNull DataKind<Map<String, Map<String, String>>> kind() {
        return LangCollector.KIND;
    }

    @Override
    public void emit(@NotNull String modId, @NotNull Map<String, Map<String, String>> data,
                     @NotNull Object platformContext) {
        if (!(platformContext instanceof GatherDataEvent event)) {
            throw new IllegalArgumentException(
                    "NeoForge lang generation requires a GatherDataEvent, got: "
                            + platformContext.getClass().getName());
        }
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();

        data.forEach((code, translations) -> {
            Map<String, String> snapshot = new LinkedHashMap<>(translations);
            generator.addProvider(true, new LanguageProvider(output, modId, code) {
                @Override
                protected void addTranslations() {
                    snapshot.forEach(this::add);
                }
            });
        });
    }
}
