package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.Placeholders;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;
import com.tngtech.java.junit.dataprovider.internal.placeholder.BasePlaceholder;

import org.junit.jupiter.api.extension.ParameterResolutionException;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Character.toUpperCase;
import static java.util.Arrays.copyOf;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;

class DataGenerator {

    private final DataConverter converter;

    DataGenerator(DataConverter converter) {
        this.converter = converter;
    }

    Data generateData(Method testMethod, Class<?> testClass) {
        UseDataProvider useDataProvider = testMethod.getAnnotation(UseDataProvider.class);
        List<DataSet> dataSets;
        if (useDataProvider == null) {
            DataProvider dataProvider = testMethod.getAnnotation(DataProvider.class);
            dataSets = singletonList(new DataSet(dataProvider, fetchData(dataProvider.value(), dataProvider, testMethod)));
        } else {
            dataSets = (useDataProvider.location().length == 0 ? Stream.of(testClass) : Stream.of(useDataProvider.location()))
                    .map(dataProviderType -> {
                        Method dataProviderMethod = findMethods(dataProviderType, dataProviderFilter(testMethod.getName(), useDataProvider.value()))
                                .stream().findFirst().orElseThrow(() -> new ParameterResolutionException("Cannot find data provider for " + testMethod));
                        Object rawData = needsExecutableParameter(dataProviderMethod) ? invokeMethod(dataProviderMethod, dataProviderType, testMethod) : invokeMethod(dataProviderMethod, dataProviderType);
                        DataProvider dataProvider = dataProviderMethod.getAnnotation(DataProvider.class);
                        return new DataSet(dataProvider, fetchData(rawData, dataProvider, testMethod));
                    })
                    .collect(toList());
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
            Set<String> methodNames = new HashSet<>();
            methodNames.add(testMethodName);
            methodNames.add(testMethodName.replaceAll("^test", "dataProvider"));
            methodNames.add(testMethodName.replaceAll("^test", "data"));
            methodNames.add("dataProvider" + toUpperCase(testMethodName.charAt(0)) + testMethodName.substring(1));
            methodNames.add("data" + toUpperCase(testMethodName.charAt(0)) + testMethodName.substring(1));
            return method -> !method.getReturnType().equals(Void.TYPE)
                    && (method.getParameterTypes().length == 0 || needsExecutableParameter(method))
                    && methodNames.contains(method.getName());
        } else {
            return method -> method.getName().equals(dataProviderName);
        }
    }

    private boolean needsExecutableParameter(Method method) {
        return method.getParameterTypes().length == 1 && Executable.class.isAssignableFrom(method.getParameterTypes()[0]);
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
