package uj.wmii.pwj.anns;

public class MyBeautifulTestSuite {

    @MyTest
    public void testSomething() {
        System.out.println("I'm testing something!");
    }

    @MyTest(params = {
        @ParamList({"test 1"}),
        @ParamList({"test 2"}),
        @ParamList({"extra cool test 3 :D"})
    })
    public void testWithParam(String param) {
        System.out.printf("I was invoked with parameter: %s\n", param);
    }

    public void notATest() {
        System.out.println("I'm not a test.");
    }

    @MyTest
    public void imFailure() {
        System.out.println("I AM EVIL.");
        throw new NullPointerException();
    }

}
