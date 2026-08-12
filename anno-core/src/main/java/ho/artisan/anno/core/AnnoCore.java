package ho.artisan.anno.core;

import ho.artisan.anno.core.validator.AnnotationValidator;
import ho.artisan.anno.core.validator.ValidatorRegistry;
import ho.artisan.anno.util.AnnoUtil;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;

public final class AnnoCore {
    private AnnoCore() {}

    // --- 包装 ---

    @NotNull
    public static Anno wrap(@NotNull AnnotatedElement element) {
        return AbstractAnno.wrap(element);
    }

    @NotNull
    public static Entry entry(@NotNull Field field) {
        return Entry.wrap(field);
    }

    @NotNull
    public static Registration registration(@NotNull Class<?> clazz) {
        return Registration.wrap(clazz);
    }

    @NotNull
    public static Instance instance(@NotNull Object obj, @NotNull Class<?> clazz) {
        return Instance.wrap(obj, clazz);
    }

    @NotNull
    public static Property property(@NotNull Object instance, @NotNull Field field) {
        return Property.wrap(instance, field);
    }

    @NotNull
    public static Invoker invoker(@NotNull Object instance, @NotNull Method method) {
        return Invoker.wrap(instance, method);
    }

    // --- 伪造注解 ---

    @NotNull
    public static <A extends Annotation> FakeAnnotation.Builder<A> fake(@NotNull Class<A> type) {
        return FakeAnnotation.builder(type);
    }

    // --- 校验 ---

    public static <A extends Annotation> void registerValidator(@NotNull AnnotationValidator<A> validator) {
        ValidatorRegistry.register(validator);
    }

    public static void validate(@NotNull Anno anno) {
        ValidatorRegistry.validate(anno);
    }

    // --- 工具 ---

    @NotNull
    public static String genID(@NotNull Anno a1, @NotNull Anno a2) {
        return AnnoUtil.genID(a1, a2);
    }

    @NotNull
    public static <A extends Anno> Comparator<A> priorityComparator() {
        return AnnoUtil.comparator();
    }
}
