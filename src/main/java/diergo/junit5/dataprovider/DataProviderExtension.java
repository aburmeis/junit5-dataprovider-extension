package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import com.tngtech.java.junit.dataprovider.internal.DataConverter;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import diergo.junit5.dataprovider.DataGenerator.Data;

/**
 * This extension creates dynamic tests. It handles test methods annotated by {@link TestTemplate}
 * and {@link DataProvider} or {@link DataProvider} and resolves the parameters.
 */
public class DataProviderExtension implements TestTemplateInvocationContextProvider, ParameterResolver {

    private static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DataProviderExtension.class);

    private final DataGenerator dataGenerator;

    /**
     * Creates an extension using the default {@link DataConverter}.
     */
    public DataProviderExtension() {
        this(new DataConverter());
    }

    /**
     * Creates an extension using the passed subclass of {@link DataConverter}. To be used by
     * default constructors of any subclass.
     */
    protected DataProviderExtension(DataConverter converter) {
        this(new DataGenerator(converter));
    }

    private DataProviderExtension(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    /**
     * Checks for any of {@link DataProvider} or {@link DataProvider} on the test method.
     */
    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return usesDataProvider(context.getRequiredTestMethod());
    }

    /**
     * Generates data for the data provider and returns invocation contexts equal to the data size.
     * The data is stored in the context store.
     */
    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Data all = dataGenerator.generateData(context.getRequiredTestMethod(), context.getRequiredTestClass());
        DataContext any = new DataContext(context.getRequiredTestMethod(), all);
        context.getStore(NAMESPACE).put(context.getRequiredTestMethod(), all);
        return IntStream.range(0, all.getSize()).mapToObj(i -> any);
    }

    /**
     * Checks for any of {@link DataProvider} or {@link DataProvider} on the {@link TestTemplate} annotated test method.
     */
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return isTestTemplate(extensionContext.getRequiredTestMethod()) && usesDataProvider(extensionContext.getRequiredTestMethod());
    }

    /**
     * Retrieves the created data from the comtext store.
     */
    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Data all = (Data) extensionContext.getStore(NAMESPACE).get(extensionContext.getRequiredTestMethod());
        return all.getData(parameterContext.getIndex() == 0)[parameterContext.getIndex()];
    }

    private boolean isTestTemplate(Executable method) {
        return method.getAnnotation(TestTemplate.class) != null;
    }

    private boolean usesDataProvider(Executable method) {
        return method.getAnnotation(DataProvider.class) != null || method.getAnnotation(UseDataProvider.class) != null;
    }

    private static class DataContext implements TestTemplateInvocationContext {
        private final Method testMethod;
        private final DataGenerator.Data data;
        private final AtomicInteger idx = new AtomicInteger(-1);

        DataContext(Method testMethod, Data data) {
            this.testMethod = testMethod;
            this.data = data;
        }

        @Override
        public String getDisplayName(int invocationIndex) {
            return data.createName(testMethod, invocationIndex - 1);
        }
    }
}
