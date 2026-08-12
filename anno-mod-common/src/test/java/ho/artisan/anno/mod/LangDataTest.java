package ho.artisan.anno.mod;

import ho.artisan.anno.core.Registration;
import ho.artisan.anno.core.annotation.ID;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LangDataTest {

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class SingleLang {
        @ID("greeting")
        @Lang(code = "en_us", text = "Hello")
        public static final String GREETING = "hello";
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class MultiLang {
        @ID("sword")
        @Lang(code = "en_us", text = "Iron Sword")
        @Lang(code = "zh_cn", text = "铁剑")
        public static final String SWORD = "sword_item";

        @ID("planks")
        @Lang(code = "en_us", text = "Wood Planks")
        public static final String PLANKS = "planks_item";
    }

    @AutoRegister("testmod")
    @SuppressWarnings("unused")
    static class NoLang {
        @ID("bare")
        public static final String BARE = "no translation";
    }

    @Test
    void singleLangCollected() {
        Map<String, Map<String, String>> data = LangData.collect(Registration.wrap(SingleLang.class));

        assertEquals(1, data.size());
        assertTrue(data.containsKey("en_us"));
        assertEquals("Hello", data.get("en_us").get("greeting"));
    }

    @Test
    void multiLangWithMultipleCodes() {
        Map<String, Map<String, String>> data = LangData.collect(Registration.wrap(MultiLang.class));

        assertEquals(2, data.size());
        assertEquals("Iron Sword", data.get("en_us").get("sword"));
        assertEquals("铁剑", data.get("zh_cn").get("sword"));
        assertEquals("Wood Planks", data.get("en_us").get("planks"));
    }

    @Test
    void noLangEntryAbsent() {
        Map<String, Map<String, String>> data = LangData.collect(Registration.wrap(NoLang.class));

        assertTrue(data.isEmpty());
    }
}
