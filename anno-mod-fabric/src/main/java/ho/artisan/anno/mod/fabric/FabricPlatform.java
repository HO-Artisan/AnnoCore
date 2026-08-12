package ho.artisan.anno.mod.fabric;

import ho.artisan.anno.mod.AnnoPlatform;
import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryKind;
import ho.artisan.anno.mod.registry.RegistryKinds;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Fabric implementation of {@link AnnoPlatform}.
 * Registered via {@code META-INF/services/ho.artisan.anno.mod.AnnoPlatform}.
 */
public final class FabricPlatform implements AnnoPlatform {

    @Override
    public @NotNull String id() {
        return "fabric";
    }

    @Override
    public @NotNull List<RegistryBinding<?>> bindings() {
        return List.of(
                FabricRegistryBinding.of(RegistryKinds.ITEM_ID, Item.class, Registries.ITEM),
                FabricRegistryBinding.of(RegistryKinds.BLOCK_ID, Block.class, Registries.BLOCK));
    }

    @Override
    public @Nullable RegistryBinding<?> bindingFor(@NotNull RegistryKind<?> kind) {
        return FabricRegistryBinding.byId(kind);
    }

    @Override
    public @NotNull List<DataEmitter<?>> emitters() {
        return List.of(new FabricLangEmitter());
    }
}
