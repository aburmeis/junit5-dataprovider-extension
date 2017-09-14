package diergo.junit5.dataprovider;

import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

public interface UsingProvidedDataGroupedByMethod {

    @TestFactory
    @ExtendWith(DataProviderExtension.class)
    default Stream<DynamicNode> ℹ︎(Stream<DataProvided> data) {
        return data.collect(groupingBy(DataProvided::getGroup, toList()))
                .entrySet().stream()
                .map(group -> dynamicContainer(group.getKey(),
                        group.getValue().stream().map(test -> dynamicTest(test.getName(), test::asExecutable))));
    }
}
