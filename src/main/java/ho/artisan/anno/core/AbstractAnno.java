package ho.artisan.anno.core;

import ho.artisan.anno.core.annotation.ID;
import ho.artisan.anno.core.annotation.Priority;
import ho.artisan.anno.exception.AnnotationNotFoundException;
import ho.artisan.anno.util.PriorityLevel;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractAnno implements Anno {
    private final Map<Class<? extends Annotation>, Annotation> annoMap;

    public AbstractAnno(@NotNull AnnotatedElement element) {
        annoMap = Arrays.stream(element.getDeclaredAnnotations()).collect(
                Collectors.toMap(Annotation::annotationType, Function.identity(), (first, second) -> first, LinkedHashMap::new)
        );
        init();
    }

    protected void init() {
        if (!contain(ID.class))
            put(FakeAnnotation.builder(ID.class).value("unnamed").build());
        if (!contain(Priority.class))
            put(FakeAnnotation.builder(Priority.class).value(PriorityLevel.LOW).build());
    }

    @Override
    public <A extends Annotation> A get(@NotNull Class<A> aClass) {
        if (!annoMap.containsKey(aClass))
            throw new AnnotationNotFoundException(aClass);
        return aClass.cast(annoMap.get(aClass));
    }

    @Override
    public <A extends Annotation> void remove(@NotNull Class<A> aClass) {
        if (!annoMap.containsKey(aClass))
            throw new AnnotationNotFoundException(aClass);
        annoMap.remove(aClass);
    }

    @Override
    public <A extends Annotation> boolean contain(@NotNull Class<A> aClass) {
        if (!annoMap.containsKey(aClass))
            return false;
        return aClass.isInstance(annoMap.get(aClass));
    }

    @Override
    public <A extends Annotation> void put(@NotNull A annotation) {
        annoMap.put(annotation.annotationType(), annotation);
    }

    @Override
    public List<? extends Annotation> annotations() {
        return new ArrayList<>(annoMap.values());
    }
}
