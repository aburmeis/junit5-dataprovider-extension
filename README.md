# junit5-dataprovider-extension

A [TestNG](http://testng.org/doc/index.html) like dataprovider extension for [JUnit 5](https://github.com/junit-team/junit5), see [junit-dataprovider](https://github.com/TNG/junit-dataprovider).


## Usage [![Download](https://api.bintray.com/packages/aburmeis/maven/junit5-dataprovider-extension/images/download.svg)](https://bintray.com/aburmeis/maven/junit5-dataprovider-extension/_latestVersion)

The extension can be found in the [JCenter](http://jcenter.bintray.com) repository. It has transitive dependencies on `junit-jupiter-api` and `junit-dataprovider`. To run the tests you will at least need an JUnit 5 engine dependency.


## Features

Supports `@DataProvider` and `@UseDataProvider` on methods with parameters annotated by `@TestTemplate`. Creates dynamic tests based on test templates.


## Limitations

* Widening calls (like data provider with `int` for a method with `long` parameter) will fail with a `ParameterResolutionException`.
* Custom resolvers of `@UseDataProvider` are ignored.


## Migration

* Replace the old JUnit annotations by JUnit5 substitutes (typically an import change like `org.junit.Test` to `org.junit.jupiter.api.TestTemplate`.
* Access to `FrameworkMethod` in `@DataProvider` has to be replaced by `Executable`.
* Replace the `@RunWith` annotation of the test class by `@ExtendWith(DataProviderExtension.class)`.


## Providing a Custom DataConverter

* Inherit from the [DataProviderExtension](src/main/java/diergo/junit5/dataprovider/DataProviderExtension.java) and pass a custom `DataConverter` to the constructor.
* Use the new extension in the `@ExtendWith` annotation.


## Contribution ![CI status](https://travis-ci.org/aburmeis/junit5-dataprovider-extension.svg)

The project is build with [Gradle](https://gradle.org/) continuously by
[Travis CI](https://travis-ci.org/aburmeis/junit5-dataprovider-extension/).
