package diergo.junit5.dataprovider;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.tngtech.java.junit.dataprovider.DataProviders.$;
import static com.tngtech.java.junit.dataprovider.DataProviders.$$;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("unused")
// This is mostly copy of DataProviderJavaAcceptanceTest.java from https://github.com/TNG/junit-dataprovider
public class DataProviderJUnit5AcceptanceTest implements UsingProvidedDataGroupedByMethod {

    @Test
    public void testAddWithoutDataProvider() {
        int result = 1 + 2;
        assertThat(result, is(3));
    }

    @DataProvider
    public static Object[][] testIsEmptyString() {
        return new Object[][]{
                {null},
                {""},
        };
    }

    @UseDataProvider
    public void testIsEmptyString(String str) {
        boolean isEmpty = str == null || str.isEmpty();
        assertThat(isEmpty, is(true));
    }

    @UseDataProvider(value = "dataProviderIsStringLengthGreaterTwo", location = StringDataProvider.class)
    public void testIsStringLengthGreaterThanTwo(String str, boolean expected) {
        boolean isGreaterThanTwo = str != null && str.length() > 2;
        assertThat(isGreaterThanTwo, is(expected));
    }

    @DataProvider
    public static Object[][] dataProviderAdd() {
        return $$(
                $(-1, -1, -2),
                $(-1, 0, -1),
                $(0, -1, -1),
                $(0, 0, 0),
                $(0, 1, 1),
                $(1, 0, 1),
                $(1, 1, 2)
        );
    }

    @UseDataProvider
    public void testAdd(int a, int b, int expected) {
        int result = a + b;
        assertThat(result, is(expected));
    }

    @DataProvider(format = "%m: %p[0] * %p[1] == %p[2]")
    public static Object[][] dataProviderMultiply() {
        return new Object[][]{
                {0, 0, 0},
                {-1, 0, 0},
                {0, 1, 0},
                {1, 1, 1},
                {1, -1, -1},
                {-1, -1, 1},
                {1, 2, 2},
                {-1, 2, -2},
                {-1, -2, 2},
                {-1, -2, 2},
                {6, 7, 42},
        };
    }

    @UseDataProvider("dataProviderMultiply")
    public void testMultiply(int a, int b, int expected) {
        assertThat(a * b, is(expected));
    }

    @DataProvider
    public static Object[][] dataProviderMinus() {
        return $$(
                $(0, 0, 0),
                $(0, 1, -1),
                $(0, -1, 1),
                $(1, 0, 1),
                $(1, 1, 0),
                $(-1, 0, -1),
                $(-1, -1, 0)
        );
    }

    @UseDataProvider("dataProviderMinus")
    public void testMinus(long a, long b, long expected) {
        long result = a - b;
        assertThat(result, is(expected));
    }

    @DataProvider
    public static Object[][] dataProviderWithNonConstantObjects() {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Calendar now = Calendar.getInstance();

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);

        return new Object[][]{
                {yesterday, yesterday, false},
                {yesterday, now, true},
                {yesterday, tomorrow, true},

                {now, yesterday, false},
                {now, now, false},
                {now, tomorrow, true},

                {tomorrow, yesterday, false},
                {tomorrow, now, false},
                {tomorrow, tomorrow, false},
        };
    }

    @UseDataProvider("dataProviderWithNonConstantObjects")
    public void testWithNonConstantObjects(Calendar cal1, Calendar cal2, boolean cal1IsEarlierThenCal2) {
        boolean result = cal1.before(cal2);
        assertThat(result, is(cal1IsEarlierThenCal2));
    }

    @DataProvider(splitBy = "\\|", trimValues = true)
    public static String[] dataProviderFileExistence() {
        return new String[]{
                "src             | true",
                "src/main        | true",
                "src/main/java/  | true",
                "src/test/java/  | true",
                "test            | false",
        };
    }

    @UseDataProvider("dataProviderFileExistence")
    public void testFileExistence(File file, boolean expected) {
        assertThat(file.exists(), is(expected));
    }

    @DataProvider
    public static List<List<Object>> dataProviderNumberFormat() {
        List<List<Object>> result = new ArrayList<List<Object>>();
        List<Object> first = new ArrayList<Object>();
        first.add(101);
        first.add("%5d");
        first.add("  101");
        result.add(first);
        List<Object> second = new ArrayList<Object>();
        second.add(125);
        second.add("%06d");
        second.add("000125");
        result.add(second);
        return result;
    }

    @UseDataProvider("dataProviderNumberFormat")
    public void testNumberFormat(Number number, String format, String expected) {
        String result = String.format(format, number);
        assertThat(result, is(expected));
    }

    @DataProvider({
            ",                 0",
            "a,                1",
            "abc,              3",
            "veryLongString,  14",
    })
    public void testStringLength(String str, int expectedLength) {
        assertThat(str.length(), is(expectedLength));
    }

    @DataProvider(value = {
            "               |  0",
            "a              |  1",
            "abc            |  3",
            "veryLongString | 14",
    }, splitBy = "\\|", trimValues = true, convertNulls = true)
    public void testStringLength2(String str, int expectedLength) {
        assertThat(str.length(), is(expectedLength));
    }

    @DataProvider({
            "0, UP",
            "1, DOWN",
            "3, FLOOR",
    })
    public void testOldModeToRoundingMode(int oldMode, RoundingMode expected) {
        assertThat(RoundingMode.valueOf(oldMode), is(expected));
    }

    @DataProvider
    public static String[] dataProviderOldModeToRoundingModeUsingRegularDataprovidert() {
        return new String[]{
                "0, UP",
                "1, DOWN",
                "3, FLOOR",
        };
    }

    @UseDataProvider("dataProviderOldModeToRoundingModeUsingRegularDataprovidert")
    public void testOldModeToRoundingModeUsingRegularDataprovider(int oldMode, RoundingMode expected) {
        assertThat(RoundingMode.valueOf(oldMode), is(expected));
    }

    @DataProvider({"null", "",})
    public void testIsEmptyString2(String str) {
        boolean isEmpty = str == null || str.isEmpty();
        assertThat(isEmpty, is(true));
    }

    @DataProvider
    public static Object[][] dataProviderWithStringContainingTabsNewlineAndCarriageReturn() {
        Object[][] result = {{}};
        return result;
    }

    @DataProvider({"Do it.\nOr let it."})
    public void testWithStringContainingTabsNewlineAndCarriageReturn(@SuppressWarnings("unused") String string) {
        // nothing to do => Just look at the test output in Eclispe's JUnit view if it is displayed correctly
    }
}
