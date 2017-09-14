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

public class DataProviderExtension implements TestTemplateInvocationContextProvider, ParameterResolver {

    private static ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(DataProviderExtension.class);

    private final DataGenerator dataGenerator;

    public DataProviderExtension() {
        this(new DataConverter());
    }

    protected DataProviderExtension(DataConverter converter) {
        this(new DataGenerator(converter));
    }

    private DataProviderExtension(DataGenerator dataGenerator) {
        this.dataGenerator = dataGenerator;
    }

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return usesDataProvider(context.getRequiredTestMethod());
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Data all = dataGenerator.generateData(context.getRequiredTestMethod(), context.getRequiredTestClass());
        DataContext any = new DataContext(context.getRequiredTestMethod(), all);
        context.getStore(NAMESPACE).put(context.getRequiredTestMethod(), all);
        return IntStream.range(0, all.getSize()).mapToObj(i -> any);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return isTestTemplate(extensionContext.getRequiredTestMethod()) && usesDataProvider(extensionContext.getRequiredTestMethod());
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        return createParameter(parameterContext, extensionContext);
    }

    private Object createParameter(ParameterContext parameterContext, ExtensionContext context) {
        Data all = (Data) context.getStore(NAMESPACE).get(context.getRequiredTestMethod());
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
