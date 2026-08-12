package ho.artisan.anno;

import ho.artisan.anno.core.Anno;
import ho.artisan.anno.core.AbstractAnno;
import ho.artisan.anno.core.FakeAnnotation;
import ho.artisan.anno.core.AnnoList;
import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;
import ho.artisan.anno.util.PriorityLevel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnoListTest {

    private static Anno anno(String id, PriorityLevel level) {
        Anno anno = AbstractAnno.wrap(Object.class);
        anno.put(FakeAnnotation.builder(ID.class).value(id).build());
        anno.put(FakeAnnotation.builder(Priority.class).value(level).build());
        return anno;
    }

    @Test
    void sortedByPriorityOrdersDescending() {
        AnnoList<Anno> list = new AnnoList<>();
        list.add(anno("low", PriorityLevel.LOW));
        list.add(anno("high", PriorityLevel.HIGH));
        list.add(anno("medium", PriorityLevel.MEDIUM));

        List<Anno> sorted = list.sortedByPriority();

        assertEquals("high", sorted.get(0).id());
        assertEquals("medium", sorted.get(1).id());
        assertEquals("low", sorted.get(2).id());
    }

    @Test
    void sortedByPriorityDoesNotLeakInternalList() {
        AnnoList<Anno> list = new AnnoList<>();
        list.add(anno("a", PriorityLevel.LOW));

        List<Anno> returned = list.sortedByPriority();
        returned.clear();

        assertEquals(1, list.size(), "clearing the returned list must not affect the AnnoList");
    }

    @Test
    void findByIdAndName() {
        AnnoList<Anno> list = new AnnoList<>();
        list.add(anno("target", PriorityLevel.LOW));

        assertEquals("target", list.findById("target").id());
        assertThrows(RuntimeException.class, () -> list.findById("missing"));
    }

    @Test
    void findByAnnotationFiltersMatches() {
        AnnoList<Anno> list = new AnnoList<>();
        list.add(anno("x", PriorityLevel.LOW));

        // every wrapped Anno carries @ID and @Priority
        assertEquals(1, list.findByAnnotation(ID.class).size());
    }

    @Test
    void removeByIdAndName() {
        AnnoList<Anno> list = new AnnoList<>();
        list.add(anno("gone", PriorityLevel.LOW));

        assertTrue(list.removeById("gone"));
        assertTrue(list.isEmpty());
    }
}
