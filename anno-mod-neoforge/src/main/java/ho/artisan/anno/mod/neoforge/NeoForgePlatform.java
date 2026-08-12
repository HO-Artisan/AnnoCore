package ho.artisan.anno.mod.neoforge;

import ho.artisan.anno.mod.AnnoPlatform;
import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryKind;
import ho.artisan.anno.mod.registry.RegistryKinds;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * NeoForge implementation of {@link AnnoPlatform}.
 * Registered via {@code META-INF/services/ho.artisan.anno.mod.AnnoPlatform}.
 */
public final class NeoForgePlatform implements AnnoPlatform {

    @Override
    public @NotNull String id() {
        return "neoforge";
    }

    @Override
    public @NotNull List<RegistryBinding<?>> bindings() {
        return List.of(
                NeoForgeRegistryBinding.of(RegistryKinds.ITEM_ID, Item.class, Registries.ITEM),
                NeoForgeRegistryBinding.of(RegistryKinds.BLOCK_ID, Block.class, Registries.BLOCK));
    }

    @Override
    public @Nullable RegistryBinding<?> bindingFor(@NotNull RegistryKind<?> kind) {
        return NeoForgeRegistryBinding.byId(kind);
    }

    @Override
    public @NotNull List<DataEmitter<?>> emitters() {
        return List.of(new NeoForgeLangEmitter());
    }
}
