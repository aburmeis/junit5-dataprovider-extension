package diergo.junit5.dataprovider;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Stream;

public interface UsingProvidedData {

    @TestFactory
    @ExtendWith(DataProviderExtension.class)
    default Stream<DynamicNode> ℹ︎(Stream<DynamicNode> data) {
        return data;
    }
}
