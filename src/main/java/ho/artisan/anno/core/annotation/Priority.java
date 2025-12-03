package ho.artisan.anno.core.annotation;

import ho.artisan.anno.util.PriorityLevel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Priority {
    PriorityLevel value();
}
