package diergo.junit5.dataprovider;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Stream;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface UsingProvidedData {

    @TestFactory
    @ExtendWith(DataProviderExtension.class)
    default Stream<DynamicNode> ℹ︎(Stream<DataProvided> data) {
        return data.map(test -> dynamicTest(test.getName(), test::asExecutable));
    }
}
