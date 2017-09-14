package diergo.junit5.dataprovider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static diergo.junit5.dataprovider.DataProviderExecutionType.FLAT;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataProviderExecution {
    DataProviderExecutionType value() default FLAT;
}
