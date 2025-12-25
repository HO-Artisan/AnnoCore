package ho.artisan.anno.core.validator;

import ho.artisan.anno.exception.AnnotationInvalidException;

import java.lang.annotation.Annotation;

public interface AnnotationValidator<A extends Annotation> {
    boolean validate(A anno) throws AnnotationInvalidException;

    Class<A> supportAnnotationType();
}
