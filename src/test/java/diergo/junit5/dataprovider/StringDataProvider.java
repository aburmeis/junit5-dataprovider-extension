package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;

// This is a copy of StringDataProvider.java from https://github.com/TNG/junit-dataprovider
public class StringDataProvider {

    @DataProvider
    public static Object[][] dataProviderIsStringLengthGreaterTwo() {
        return new Object[][]{
                {"", false},
                {"1", false},
                {"12", false},
                {"123", true},
                {"Test", true},
        };
    }
}
