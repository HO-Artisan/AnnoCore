package ho.artisan.anno.mod.fabric;

import ho.artisan.anno.mod.LangCollector;
import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.data.DataKind;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Writes {@link LangCollector} output to Fabric language files, one
 * {@link FabricLanguageProvider} per language code.
 */
public final class FabricLangEmitter implements DataEmitter<Map<String, Map<String, String>>> {

    @Override
    public @NotNull DataKind<Map<String, Map<String, String>>> kind() {
        return LangCollector.KIND;
    }

    @Override
    public void emit(@NotNull String modId, @NotNull Map<String, Map<String, String>> data,
                     @NotNull Object platformContext) {
        if (!(platformContext instanceof FabricDataGenerator.Pack pack)) {
            throw new IllegalArgumentException(
                    "Fabric lang generation requires a FabricDataGenerator.Pack, got: "
                            + platformContext.getClass().getName());
        }

        data.forEach((code, translations) -> {
            Map<String, String> snapshot = new LinkedHashMap<>(translations);
            pack.addProvider((FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> lookup) ->
                    new FabricLanguageProvider(output, code, lookup) {
                        @Override
                        public void generateTranslations(RegistryWrapper.WrapperLookup regLookup,
                                                         TranslationBuilder builder) {
                            snapshot.forEach(builder::add);
                        }
                    });
        });
    }
}
