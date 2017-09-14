package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.Placeholders;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;

import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

class TestGenerator {
    private final DataConverter dataConverter;

    TestGenerator(DataConverter dataConverter) {
        this.dataConverter = dataConverter;
    }

    Stream<DataProvided> generateExplodedTestMethodsFor(Method testMethod, Object target) {
        MethodHandles.Lookup lookup = MethodHandles.publicLookup().in(target.getClass());
        UseDataProvider useDataProvider = testMethod.getAnnotation(UseDataProvider.class);
        List<Object[]> data;
        DataProvider dataProvider;
        if (useDataProvider == null) {
            dataProvider = testMethod.getAnnotation(DataProvider.class);
            data = fetchData(dataProvider.value(), dataProvider, testMethod);
        } else {
            Class<?> dataProviderType = useDataProvider.location().length == 0 ? target.getClass() : useDataProvider.location()[0];
            Method dataProviderMethod = findMethods(dataProviderType, dataProviderFilter(testMethod.getName(), useDataProvider.value()))
                    .stream().findFirst().orElseThrow(
                            () -> new ParameterResolutionException("Cannot find data provider for " + testMethod));
            dataProvider = dataProviderMethod.getAnnotation(DataProvider.class);
            data = fetchData(invokeMethod(dataProviderMethod, dataProviderType), dataProvider, testMethod);
        }
        try {
            String format = dataProvider.format();
            MethodHandle handle = lookup.unreflect(testMethod).bindTo(target);
            return IntStream.range(0, data.size())
                    .mapToObj(i -> new DataProvided(testMethod.getName(), createName(format, i, testMethod, data.get(i)), handle, data.get(i)));
        } catch (IllegalAccessException e) {
            throw new ParameterResolutionException("Cannot access test method " + testMethod, e);
        }

    }

    private String createName(String format, int idx, Method testMethod, Object... data) {
        String result = format;
        for (BasePlaceholder placeHolder : Placeholders.all()) {
            placeHolder.setContext(testMethod, idx, Arrays.copyOf(data, data.length));
            result = placeHolder.process(result);
        }
        return result;
    }

    private List<Object[]> fetchData(Object data, DataProvider dataProvider, Method testMethod) {
        try {
            return dataConverter.convert(data, testMethod.isVarArgs(), testMethod.getParameterTypes(), dataProvider);
        } catch (RuntimeException e) {
            throw new ParameterResolutionException("Cannot convert data provided for " + testMethod, e);
        }
    }

    private Predicate<Method> dataProviderFilter(String testMethodName, String dataProviderName) {
        if (dataProviderName.equals(UseDataProvider.DEFAULT_VALUE)) {
            List<String> names = new ArrayList<>();
            names.add(testMethodName);
            if (testMethodName.startsWith("test")) {
                names.add("data" + testMethodName.substring(4));
                names.add("dataProvider" + testMethodName.substring(4));
            }
            return method -> !method.getReturnType().equals(Void.TYPE) && names.contains(method.getName());
        } else {
            return method -> method.getName().equals(dataProviderName);
        }
    }
}
