package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;

import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.stream.Stream;

import static java.util.function.Function.identity;

public class DataProviderExtension implements ParameterResolver {

    private final TestGenerator testGenerator;

    public DataProviderExtension() {
        this(new DataConverter());
    }

    protected DataProviderExtension(DataConverter dataConverter) {
        this(new TestGenerator(dataConverter));
    }

    DataProviderExtension(TestGenerator testGenerator) {
        this.testGenerator = testGenerator;
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return isTestFactory(parameterContext.getDeclaringExecutable())
                && needsDataProvided(parameterContext.getParameter());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Object target = parameterContext.getTarget().orElseThrow(
                () -> new ParameterResolutionException("Missing target for " + parameterContext));
        return Stream.of(target.getClass().getMethods())
                .filter(this::usesDataProvider)
                .map(method -> testGenerator.generateExplodedTestMethodsFor(method, target))
                .flatMap(identity());
    }

    private boolean isTestFactory(Executable method) {
        return method.getAnnotation(TestFactory.class) != null;
    }

    private boolean needsDataProvided(Parameter parameter) {
        if (Stream.class.equals(parameter.getType())) {
            ParameterizedType parameterizedType = (ParameterizedType) parameter.getParameterizedType();
            Type firstGenericArgument = parameterizedType.getActualTypeArguments()[0];
            return DataProvided.class.equals(firstGenericArgument);
        }
        return false;
    }

    private boolean usesDataProvider(Method method) {
        return method.getReturnType().equals(Void.TYPE) &&
                (method.getAnnotation(DataProvider.class) != null || method.getAnnotation(UseDataProvider.class) != null);
    }
}
