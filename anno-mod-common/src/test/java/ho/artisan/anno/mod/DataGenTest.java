package ho.artisan.anno.mod;

import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.mod.data.DataCollector;
import ho.artisan.anno.mod.data.DataEmitter;
import ho.artisan.anno.mod.data.DataGenRegistry;
import ho.artisan.anno.mod.data.DataKind;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Exercises the collector/emitter pipeline with a custom kind — the same path a
 * third-party recipe or tag generator would take, with no platform on the classpath.
 */
class DataGenTest {

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Items {
        @ID("sword")
        @Lang(code = "en_us", text = "Iron Sword")
        public static final String SWORD = "sword_item";
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class Blocks {
        @ID("stone")
        @Lang(code = "en_us", text = "Stone")
        @Lang(code = "zh_cn", text = "石头")
        public static final String STONE = "stone_block";
    }

    /** Collects each entry's id, so the test can assert on collector input. */
    record Ids(String modId, List<String> ids) {}

    static final class IdCollector implements DataCollector<Ids> {
        static final DataKind<Ids> KIND = DataKind.of("test:ids");

        @Override
        public @NotNull DataKind<Ids> kind() {
            return KIND;
        }

        @Override
        public @NotNull Ids collect(@NotNull String modId, @NotNull List<Registration> registrations) {
            List<String> ids = new ArrayList<>();
            registrations.forEach(reg -> reg.entries().getAll().forEach(e -> ids.add(e.id())));
            return new Ids(modId, ids);
        }
    }

    /** Captures what it was handed instead of writing files. */
    static final class CapturingEmitter<D> implements DataEmitter<D> {
        private final DataKind<D> kind;
        D received;
        Object context;
        int calls;

        CapturingEmitter(DataKind<D> kind) {
            this.kind = kind;
        }

        @Override
        public @NotNull DataKind<D> kind() {
            return kind;
        }

        @Override
        public void emit(@NotNull String modId, @NotNull D data, @NotNull Object platformContext) {
            this.received = data;
            this.context = platformContext;
            this.calls++;
        }
    }

    @Test
    void customKindFlowsFromCollectorToEmitter() {
        CapturingEmitter<Ids> emitter = new CapturingEmitter<>(IdCollector.KIND);
        DataGenRegistry.register(emitter);
        Object ctx = new Object();

        AnnoMod.create("testmod")
                .data(new IdCollector(), Items.class)
                .generate(ctx);

        assertEquals(1, emitter.calls);
        assertSame(ctx, emitter.context);
        assertEquals("testmod", emitter.received.modId());
        assertEquals(List.of("sword"), emitter.received.ids());
    }

    @Test
    void repeatedQueuingMergesIntoOneEmit() {
        CapturingEmitter<Ids> emitter = new CapturingEmitter<>(IdCollector.KIND);
        DataGenRegistry.register(emitter);
        IdCollector collector = new IdCollector();

        AnnoMod.create("testmod")
                .data(collector, Items.class)
                .data(collector, Blocks.class)
                .generate(new Object());

        assertEquals(1, emitter.calls, "one emit per kind, not per class");
        assertEquals(List.of("sword", "stone"), emitter.received.ids());
    }

    @Test
    void langUsesTheSamePipeline() {
        CapturingEmitter<Map<String, Map<String, String>>> emitter =
                new CapturingEmitter<>(LangCollector.KIND);
        DataGenRegistry.register(emitter);

        AnnoMod.create("testmod").lang(Items.class).lang(Blocks.class).generate(new Object());

        assertEquals(1, emitter.calls);
        assertEquals("Iron Sword", emitter.received.get("en_us").get("sword"));
        assertEquals("Stone", emitter.received.get("en_us").get("stone"));
        assertEquals("石头", emitter.received.get("zh_cn").get("stone"));
    }

    @Test
    void missingEmitterFailsWithKindInMessage() {
        DataKind<String> orphan = DataKind.of("test:orphan");
        DataCollector<String> collector = new DataCollector<>() {
            @Override
            public @NotNull DataKind<String> kind() {
                return orphan;
            }

            @Override
            public @NotNull String collect(@NotNull String modId, @NotNull List<Registration> registrations) {
                return "";
            }
        };

        AnnoMod mod = AnnoMod.create("testmod").data(collector, Items.class);
        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> mod.generate(new Object()));
        assertTrue(thrown.getMessage().contains("test:orphan"), thrown.getMessage());
    }

    @Test
    void conflictingCollectorsForOneKindRejected() {
        AnnoMod mod = AnnoMod.create("testmod").data(new IdCollector(), Items.class);

        assertThrows(IllegalArgumentException.class,
                () -> mod.data(new IdCollector(), Blocks.class),
                "distinct instances claiming one kind is ambiguous");
    }

    @Test
    void generateWithNothingQueuedIsNoOp() {
        assertDoesNotThrow(() -> AnnoMod.create("testmod").generate(new Object()));
    }

    @Test
    void kindIdentityIsByIdAlone() {
        assertEquals(DataKind.of("lang"), DataKind.<String>of("lang"));
        assertEquals(DataKind.of("lang").hashCode(), DataKind.of("lang").hashCode());
        assertNotEquals(DataKind.of("lang"), DataKind.of("recipes"));
    }
}
