package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.Placeholders;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;

import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static java.util.Arrays.copyOf;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

class DataGenerator {

    private final DataConverter converter;

    DataGenerator(DataConverter converter) {
        this.converter = converter;
    }

    Data generateData(Method testMethod, Class<?> testClass) {
        UseDataProvider useDataProvider = testMethod.getAnnotation(UseDataProvider.class);
        List<DataSet> dataSets = new ArrayList<>();
        if (useDataProvider == null) {
            DataProvider dataProvider = testMethod.getAnnotation(DataProvider.class);
            dataSets.add(new DataSet(dataProvider, fetchData(dataProvider.value(), dataProvider, testMethod)));
        } else {
            Class<?> dataProviderType = useDataProvider.location().length == 0 ? testClass : useDataProvider.location()[0];
            Method dataProviderMethod = findMethods(dataProviderType, dataProviderFilter(testMethod.getName(), useDataProvider.value()))
                    .stream().findFirst().orElseThrow(
                            () -> new ParameterResolutionException("Cannot find data provider for " + testMethod));
            boolean passMethod = dataProviderMethod.getParameterCount() == 1 && Executable.class.isAssignableFrom(dataProviderMethod.getParameterTypes()[0]);
            Object rawData = passMethod ? invokeMethod(dataProviderMethod, dataProviderType, testMethod) : invokeMethod(dataProviderMethod, dataProviderType);
            DataProvider dataProvider = dataProviderMethod.getAnnotation(DataProvider.class);
            dataSets.add(new DataSet(dataProvider, fetchData(rawData, dataProvider, testMethod)));
        }
        return new Data(dataSets);
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
        private final List<DataSet> dataSets;
        private final AtomicInteger i;

        Data(List<DataSet> dataSets) {
            this.dataSets = dataSets;
            i = new AtomicInteger(-1);
        }

        int getSize() {
            return dataSets.stream().mapToInt(data -> data.data.size()).sum();
        }

        Object[] getData(boolean firstParam) {
            int idx = firstParam ? this.i.incrementAndGet() : this.i.get();
            DataSet set = findDataSet(idx);
            return set.data.get(findSubIndex(idx));
        }

        String createName(Method testMethod, int idx) {
            DataSet set = findDataSet(idx);
            String result = set.provider.format();
            for (BasePlaceholder placeHolder : Placeholders.all()) {
                Object[] data = set.data.get(findSubIndex(idx));
                placeHolder.setContext(testMethod, idx, copyOf(data, data.length));
                result = placeHolder.process(result);
            }
            return result;
        }

        private DataSet findDataSet(int idx) {
            int i = idx;
            for (DataSet set : dataSets) {
                if (i < set.data.size()) {
                    return set;
                }
                i -= set.data.size();
            }
            throw new IndexOutOfBoundsException("No data at index " + idx);
        }

        private int findSubIndex(int idx) {
            for (DataSet set : dataSets) {
                if (idx < set.data.size()) {
                    return idx;
                }
                idx -= set.data.size();
            }
            return -1;
        }
    }

    static class DataSet {
        private final DataProvider provider;
        private final List<Object[]> data;

        DataSet(DataProvider provider, List<Object[]> data) {
            this.provider = provider;
            this.data = data;
        }
    }
}
