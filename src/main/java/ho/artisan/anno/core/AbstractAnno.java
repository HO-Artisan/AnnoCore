package ho.artisan.anno.core;

import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ho.artisan.anno.util.PriorityLevel.LOW;

public abstract class AbstractAnno implements Anno {
    private final Map<Class<? extends Annotation>, Annotation> map;

    public AbstractAnno(AnnotatedElement element) {
        map = Arrays.stream(element.getDeclaredAnnotations()).collect(
                Collectors.toMap(Annotation::annotationType, Function.identity(), (first, second) -> first, LinkedHashMap::new)
        );
        init();
    }

    protected void init() {
        if (!contain(ID.class))
            put(FakeAnnotation.builder(ID.class).value("unnamed").build());
        if (!contain(Priority.class))
            put(FakeAnnotation.builder(Priority.class).value(LOW).build());
    }

    @Override
    public <A extends Annotation> A get(Class<A> aClass) {
        return aClass.cast(map.get(aClass));
    }

    @Override
    public <A extends Annotation> boolean contain(Class<A> aClass) {
        if (!map.containsKey(aClass))
            return false;
        return aClass.isInstance(map.get(aClass));
    }

    @Override
    public <A extends Annotation> void put(A annotation) {
        map.put(annotation.annotationType(), annotation);
    }

    @Override
    public <A extends Annotation> void remove(Class<A> aClass) {
        map.remove(aClass);
    }

    @Override
    public List<? extends Annotation> annotations() {
        return new ArrayList<>(map.values());
    }
}
