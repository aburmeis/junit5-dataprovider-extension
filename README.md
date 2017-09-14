# junit5-dataprovider-extension

[JUnit5](https://github.com/junit-team/junit5) extension to use [data providers](https://github.com/TNG/junit-dataprovider) easily.


## Features

Supports `@DataProvider` and `@UseDataProvider` on methods with parameters annotated by `@TestTemplate`. Creates dynamic tests based on test templates.


## Limitations

* Access to `FrameworkMethod` in `@DataProvider` is not supported.
* Widening calls (like data provider with `int` for a method with `long` parameter will fail with a `ParameterResolutionException`.


## Migration

* Replace the old JUnit annotations by JUnit5 substitutes (typically an import change like `org.junit.Test` to `org.junit.jupiter.api.TestTemplate`.
* Replace the `@RunWith` annotation of the test class by `@ExtendWith(DataProviderExtension.class)`.


## Providing a Custom DataConverter

* Inherit from the [DataProviderExtension](src/main/java/diergo/junit5/dataprovider/DataProviderExtension.java) and pass a custom `DataConverter` to the constructor.
* Use the new extension in the `@ExtendWith` annotation.
