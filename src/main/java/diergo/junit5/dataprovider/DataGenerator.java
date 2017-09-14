package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.Placeholders;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;

import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

class DataGenerator {

    private final DataConverter converter;

    DataGenerator(DataConverter converter) {
        this.converter = converter;
    }

    Data generateData(Method testMethod, Class<?> testClass) {
        UseDataProvider useDataProvider = testMethod.getAnnotation(UseDataProvider.class);
        List<Object[]> data;
        DataProvider dataProvider;
        if (useDataProvider == null) {
            dataProvider = testMethod.getAnnotation(DataProvider.class);
            data = fetchData(dataProvider.value(), dataProvider, testMethod);
        } else {
            Class<?> dataProviderType = useDataProvider.location().length == 0 ? testClass : useDataProvider.location()[0];
            Method dataProviderMethod = findMethods(dataProviderType, dataProviderFilter(testMethod.getName(), useDataProvider.value()))
                    .stream().findFirst().orElseThrow(
                            () -> new ParameterResolutionException("Cannot find data provider for " + testMethod));
            dataProvider = dataProviderMethod.getAnnotation(DataProvider.class);
            data = fetchData(invokeMethod(dataProviderMethod, dataProviderType), dataProvider, testMethod);
        }
        return new Data(dataProvider, data);
    }

    private List<Object[]> fetchData(Object data, DataProvider dataProvider, Method testMethod) {
        try {
            return converter.convert(data, testMethod.isVarArgs(), testMethod.getParameterTypes(), dataProvider);
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

    static class Data {
        private final DataProvider provider;
        private final List<Object[]> data;
        private final AtomicInteger i = new AtomicInteger(-1);

        Data(DataProvider provider, List<Object[]> data) {
            this.provider = provider;
            this.data = data;
        }

        int getSize() {
            return data.size();
        }

        Object[] getData(boolean firstParam) {
            return data.get(firstParam ? i.incrementAndGet() : i.get());
        }

        String createName(Method testMethod, int idx) {
            String result = provider.format();
            for (BasePlaceholder placeHolder : Placeholders.all()) {
                placeHolder.setContext(testMethod, idx, Arrays.copyOf(data.get(idx), data.get(idx).length));
                result = placeHolder.process(result);
            }
            return result;
        }
    }
}
