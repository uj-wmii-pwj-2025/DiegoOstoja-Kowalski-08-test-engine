package uj.wmii.pwj.anns;

public class ArithmeticTestSuite {
    @MyTest(params = {
            @ParamList({"1", "2"}),
            @ParamList({"42", "420"}),
            @ParamList({"-100", "99"})
    },
            expected = {"3", "462", "-1"}
    )
    public int testAdd(Integer a, Integer b) {
        return a + b;
    }

    @MyTest(expected = {"12"})
    public int testGetTwelve() {
        return 12;
    }

    @MyTest(params = {
            @ParamList({"-3", "4", "2"}),
            @ParamList({"4", "5", "6"})
    },
            expected = {"-24", "120"}
    )
    public int testMultiplyWrongAnswer(int a, int b, int c) {
        return  (a - 1) * (b - 1) * c;
    }

    @MyTest(params = {
            @ParamList({"0", "1", "2"}),
            @ParamList({"4", "5", "6"})
    })
    public void testMultiplyVoid(int a, int b, int c) {
        int result = a * b * c;
    }

    @MyTest(params = {
            @ParamList({"0", "1"}),
            @ParamList({"27", "16"}),
            @ParamList({"1", "0"})
    })
    public void testDivideError(int a, int b) {
        int result = a / b;
    }
}
