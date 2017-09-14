# junit5-dataprovider-extension

[JUnit5](https://github.com/junit-team/junit5) extension to use [data providers](https://github.com/TNG/junit-dataprovider) easily.


## Features

Supports `@DataProvider` and `@UseDataProvider` by creating `DynamicTest` instances out of it.


## Limitations

Access to `FrameworkMethod` in `@DataProvider` is not supported.


## Migration

* Replace the old JUnit annotations by JUnit5 substitutes (typically an import change like `org.junit.Test` to `org.junit.jupiter.api.Test`.
* Remove the `@RunWith` annotation from the test.
* Let the test implement `UsingProvidedData`.
* To group tests by method annotate the test by `@DataProviderExecution(GROUPED)`.


## Providing a Custom DataConverter

* Inherit from the [DataProviderExtension](src/main/java/diergo/junit5/dataprovider/DataProviderExtension.java) and pass a custom `DataConverter` to the constructor.
* Write a method similar to that in [UsingProvidedData](src/main/java/diergo/junit5/dataprovider/UsingProvidedData.java) replacing the extension class by your own.
