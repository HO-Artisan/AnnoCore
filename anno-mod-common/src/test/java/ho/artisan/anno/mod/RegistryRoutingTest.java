package ho.artisan.anno.mod;

import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;
import ho.artisan.anno.mod.registry.RegisterTo;
import ho.artisan.anno.mod.registry.RegistryBinding;
import ho.artisan.anno.mod.registry.RegistryBindings;
import ho.artisan.anno.mod.registry.RegistryEntry;
import ho.artisan.anno.mod.registry.RegistryKind;
import ho.artisan.anno.util.PriorityLevel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises entry → registry routing with stand-in types, so items and blocks get no special
 * treatment here either — the same paths a modded registry would take.
 */
class RegistryRoutingTest {

    // Stand-ins for Item / Block, plus one value that is both (as BlockItem is).
    static class FakeItem {}
    static class FakeBlock {}
    static class FakeBlockItem extends FakeItem {}
    static class FakeSpell {}

    static final String ITEM_ID = "test:item";
    static final String BLOCK_ID = "test:block";
    static final String SPELL_ID = "test:spell";

    /** Records what it was asked to register instead of touching a registry. */
    static final class CapturingBinding<T> implements RegistryBinding<T> {
        private final RegistryKind<T> kind;
        private final Class<T> type;
        final List<String> ids = new ArrayList<>();
        Object context;
        int calls;

        CapturingBinding(String registryId, @Nullable Class<T> type) {
            this.kind = RegistryKind.of(registryId);
            this.type = type;
        }

        @Override
        public @NotNull RegistryKind<T> kind() {
            return kind;
        }

        @Override
        public @Nullable Class<T> type() {
            return type;
        }

        @Override
        public void register(@NotNull String modId, @NotNull List<RegistryEntry<T>> entries, Object platformContext) {
            entries.forEach(e -> ids.add(e.id()));
            this.context = platformContext;
            this.calls++;
        }
    }

    private static CapturingBinding<FakeItem> bindItems() {
        CapturingBinding<FakeItem> binding = new CapturingBinding<>(ITEM_ID, FakeItem.class);
        RegistryBindings.register(binding);
        return binding;
    }

    private static CapturingBinding<FakeBlock> bindBlocks() {
        CapturingBinding<FakeBlock> binding = new CapturingBinding<>(BLOCK_ID, FakeBlock.class);
        RegistryBindings.register(binding);
        return binding;
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Mixed {
        @ID("sword") public static final FakeItem SWORD = new FakeItem();
        @ID("stone") public static final FakeBlock STONE = new FakeBlock();
        @ID("version") public static final String VERSION = "1.0";   // not registrable
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Priorities {
        @ID("low") @Priority(PriorityLevel.LOW) public static final FakeItem LOW = new FakeItem();
        @ID("high") @Priority(PriorityLevel.HIGH) public static final FakeItem HIGH = new FakeItem();
        @ID("medium") @Priority(PriorityLevel.MEDIUM) public static final FakeItem MEDIUM = new FakeItem();
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Annotated {
        @ID("fireball")
        @RegisterTo(SPELL_ID)
        public static final FakeSpell FIREBALL = new FakeSpell();
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Ambiguous {
        @ID("oak_door") public static final FakeBlockItem DOOR = new FakeBlockItem();
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Disambiguated {
        @ID("oak_door")
        @RegisterTo(ITEM_ID)
        public static final FakeBlockItem DOOR = new FakeBlockItem();
    }

    @Test
    void mixedContainerRoutesByType() {
        CapturingBinding<FakeItem> items = bindItems();
        CapturingBinding<FakeBlock> blocks = bindBlocks();
        Object ctx = new Object();

        AnnoMod.create("testmod").register(Mixed.class).commit(ctx);

        assertEquals(List.of("sword"), items.ids);
        assertEquals(List.of("stone"), blocks.ids);
        assertSame(ctx, items.context);
    }

    @Test
    void unroutableEntrySkippedNotFailed() {
        bindItems();
        bindBlocks();

        assertDoesNotThrow(() -> AnnoMod.create("testmod").register(Mixed.class).commit(null),
                "a plain String constant must not break the commit");
    }

    @Test
    void forcedKindFiltersByBindingType() {
        CapturingBinding<FakeItem> items = bindItems();
        CapturingBinding<FakeBlock> blocks = bindBlocks();

        // Same container queued for both kinds — the documented items()/blocks() pattern.
        AnnoMod.create("testmod")
                .register(items.kind(), Mixed.class)
                .register(blocks.kind(), Mixed.class)
                .commit(null);

        assertEquals(List.of("sword"), items.ids);
        assertEquals(List.of("stone"), blocks.ids);
    }

    @Test
    void registerToReachesUnlistedRegistry() {
        CapturingBinding<FakeSpell> spells = new CapturingBinding<>(SPELL_ID, null);
        RegistryBindings.register(spells);
        bindItems();

        AnnoMod.create("testmod").register(Annotated.class).commit(null);

        assertEquals(List.of("fireball"), spells.ids);
    }

    @Test
    void entriesArrivePriorityOrderedInOneCall() {
        CapturingBinding<FakeItem> items = bindItems();

        AnnoMod.create("testmod").register(Priorities.class).commit(null);

        assertEquals(1, items.calls, "one call per kind, so bindings can batch");
        assertEquals(List.of("high", "medium", "low"), items.ids);
    }

    @Test
    void ambiguousTypeFailsWithCandidates() {
        bindItems();
        CapturingBinding<FakeBlockItem> alsoItem = new CapturingBinding<>("test:also_item", FakeBlockItem.class);
        RegistryBindings.register(alsoItem);

        AnnoMod mod = AnnoMod.create("testmod").register(Ambiguous.class);
        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> mod.commit(null));
        assertTrue(thrown.getMessage().contains(ITEM_ID), thrown.getMessage());
        assertTrue(thrown.getMessage().contains("@RegisterTo"), thrown.getMessage());
    }

    @Test
    void registerToResolvesAmbiguity() {
        CapturingBinding<FakeItem> items = bindItems();
        RegistryBindings.register(new CapturingBinding<>("test:also_item", FakeBlockItem.class));

        AnnoMod.create("testmod").register(Disambiguated.class).commit(null);

        assertEquals(List.of("oak_door"), items.ids);
    }

    @Test
    void missingBindingFailsWithKindInMessage() {
        AnnoMod mod = AnnoMod.create("testmod")
                .register(RegistryKind.of("test:absent"), Mixed.class);

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> mod.commit(null));
        assertTrue(thrown.getMessage().contains("test:absent"), thrown.getMessage());
    }

    @Test
    void commitWithNothingQueuedIsNoOp() {
        assertDoesNotThrow(() -> AnnoMod.create("testmod").commit(null));
    }

    /**
     * The linchpin of the Architectury add-on: installing a binding for a kind the active
     * platform also binds must <em>replace</em> it, not compete with it — otherwise inference
     * would see two candidates for one type and refuse to route.
     */
    @Test
    void explicitBindingReplacesEarlierOneForSameKind() {
        String kindId = "test:replaceable";
        CapturingBinding<FakeSpell> first = new CapturingBinding<>(kindId, FakeSpell.class);
        CapturingBinding<FakeSpell> second = new CapturingBinding<>(kindId, FakeSpell.class);
        RegistryBindings.register(first);
        RegistryBindings.register(second);

        assertSame(second, RegistryBindings.require(RegistryKind.<FakeSpell>of(kindId)),
                "last registration for a kind wins");
        long forKind = RegistryBindings.typed().stream()
                .filter(b -> b.kind().id().equals(kindId))
                .count();
        assertEquals(1, forKind, "one candidate per kind, so inference stays unambiguous");
    }

    @Test
    void untypedBindingStaysOutOfInference() {
        String kindId = "test:untyped";
        RegistryBindings.register(new CapturingBinding<>(kindId, null));

        assertTrue(RegistryBindings.typed().stream().noneMatch(b -> b.kind().id().equals(kindId)),
                "a binding with no declared type must never be inferred");
        assertNotNull(RegistryBindings.find(RegistryKind.of(kindId)),
                "but it is still reachable by explicit kind");
    }

    @Test
    void kindIdentityIsByIdAlone() {
        assertEquals(RegistryKind.of(ITEM_ID), RegistryKind.<FakeItem>of(ITEM_ID));
        assertEquals(RegistryKind.of(ITEM_ID).hashCode(), RegistryKind.of(ITEM_ID).hashCode());
        assertNotEquals(RegistryKind.of(ITEM_ID), RegistryKind.of(BLOCK_ID));
    }
}
