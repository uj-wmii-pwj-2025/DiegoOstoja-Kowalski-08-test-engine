package uj.wmii.pwj.anns;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MyTestEngine {

    private final String className;

    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(
            boolean.class, Boolean.class,
            byte.class, Byte.class,
            char.class, Character.class,
            short.class, Short.class,
            int.class, Integer.class,
            long.class, Long.class,
            float.class, Float.class,
            double.class, Double.class
    );

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please specify test class name");
            System.exit(-1);
        }
        String className = args[0].trim();
        System.out.printf("Testing class: %s\n", className);
        System.out.println(
                " _____     _____         _   _____         _         \n" +
                "|     |_ _|_   _|___ ___| |_|   __|___ ___|_|___ ___ \n" +
                "| | | | | | | | | -_|_ -|  _|   __|   | . | |   | -_|\n" +
                "|_|_|_|_  | |_| |___|___|_| |_____|_|_|_  |_|_|_|___|\n" +
                "      |___|                           |___|  ");
        MyTestEngine engine = new MyTestEngine(className);
        engine.runTests();
    }

    public MyTestEngine(String className) {
        this.className = className;
    }

    public void runTests() {
        final Object unit = getObject(className);
        List<Method> testMethods = getTestMethods(unit);
        for (Method m: testMethods) {
            printSingleMethod(m);
        }

        int successCount = 0;
        int failCount = 0;
        int errorCount = 0;
        for (Method m: testMethods) {
            TestResult result = launchSingleMethod(m, unit);
            if (result == TestResult.PASS) successCount++;
            else if (result == TestResult.FAIL) failCount++;
            else errorCount++;
        }
        System.out.printf("Engine launched %d tests.\n", testMethods.size());
        System.out.printf("%d of them passed, %d failed, and %d ended in an error.\n", successCount, failCount, errorCount);
    }

    private void printSingleMethod(Method m) {
        ParamList[] params = m.getAnnotation(MyTest.class).params();
        int paramsLength = params.length;
        String[][] paramsStrings = new String[paramsLength][];
        for (int i = 0; i < paramsLength; i++) {
            paramsStrings[i] = params[i].value();
        }
        String[] expected = m.getAnnotation(MyTest.class).expected();

        System.out.println("Planned test of method: " + m.getName());
        System.out.println("Received parameters: " + Arrays.deepToString(paramsStrings));
        System.out.println("Received expected outputs: " + Arrays.toString(expected));
        System.out.println();
    }

    private TestResult launchSingleMethod(Method m, Object unit) {
        String[] lastParameters = new String[]{};
        try {
            boolean detectedFails = false;
            ParamList[] params = m.getAnnotation(MyTest.class).params();
            int paramsLength = params.length;
            String[][] paramsStrings = new String[paramsLength][];
            for (int i = 0; i < paramsLength; i++) {
                paramsStrings[i] = params[i].value();
            }
            String[] expected = m.getAnnotation(MyTest.class).expected();
            int expectedLength = expected.length;
            Class<?>[] parameterTypes = primitiveToWrapperList(m.getParameterTypes());
            Class<?> returnType = primitiveToWrapper(m.getReturnType());

            if(!checkLengths(paramsStrings, parameterTypes)) {
                System.out.println("Parameter list error in method: " + m.getName()
                        + ", a parameter list has wrong length");
                return TestResult.ERROR;
            }

            if (paramsLength == 0 && expectedLength == 0) {
                lastParameters = new String[]{};
                m.invoke(unit);
            }
            else if (paramsLength == 0 && expectedLength == 1) {
                lastParameters = new String[]{};
                Object result = m.invoke(unit);
                if (!checkResult(result, returnType, expected[0])) {
                    detectedFails = true;
                    System.out.println("Tested method: " + m.getName()
                            + ", expected result: " + expected[0]
                            + ", found result: " + returnType.cast(result).toString());
                }
            }
            else if (paramsLength > 0 && expectedLength == 0) {
                Object[] convertedParams;
                for (String[] paramList : paramsStrings) {
                    lastParameters = paramList;
                    convertedParams = convertParams(paramList, parameterTypes);
                    m.invoke(unit, convertedParams);
                }
            }
            else if (paramsLength == expectedLength) {
                Object[] convertedParams;
                Object result;
                for (int i = 0; i < params.length; i++){
                    String[] paramList = paramsStrings[i];
                    lastParameters = paramList;
                    convertedParams = convertParams(paramList, parameterTypes);
                    result = m.invoke(unit, convertedParams);
                    if (!checkResult(result, returnType, expected[i])) {
                        detectedFails = true;
                        System.out.println("Failed test: " + m.getName()
                                + ", parameters: " + Arrays.toString(paramList)
                                + ", expected result: " + expected[i]
                                + ", found result: " + returnType.cast(result).toString());
                    }
                }
            }
            else {
                System.out.println("Parameter list error in method: " + m.getName()
                        + ", parameters and expected results have different lengths");
                return TestResult.ERROR;
            }

            if (detectedFails) {
                System.out.println("Tested method: " + m.getName()
                        + ", test unsuccessful, some tests failed.");
                return TestResult.FAIL;
            }
            else {
                System.out.println("Tested method: " + m.getName() + ", test successful.");
                return TestResult.PASS;
            }
        } catch (ReflectiveOperationException e) {
            System.out.println("An error occurred in method: " + m.getName()
                    + " for parameters: " + Arrays.toString(lastParameters));
            e.printStackTrace();
            return TestResult.ERROR;
        } catch (IllegalArgumentException e) {
            System.out.println("Parameter conversion error in method: " + m.getName()
                    + " for parameters: " + Arrays.toString(lastParameters));
            e.printStackTrace();
            return TestResult.ERROR;
        }
    }

    private static List<Method> getTestMethods(Object unit) {
        Method[] methods = unit.getClass().getDeclaredMethods();
        return Arrays.stream(methods).filter(
                m -> m.getAnnotation(MyTest.class) != null).collect(Collectors.toList());
    }

    private static Object getObject(String className) {
        try {
            Class<?> unitClass = Class.forName(className);
            return unitClass.getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            return new Object();
        }
    }

    private static Class<?> primitiveToWrapper(Class<?> type) {
        if (type.isPrimitive()) {
            return PRIMITIVE_TO_WRAPPER.get(type);
        }
        return type;
    }

    private static Class<?>[] primitiveToWrapperList(Class<?>[] types) {
        return Arrays.stream(types).map(MyTestEngine::primitiveToWrapper
        ).toArray(Class<?>[]::new);
    }

    private static boolean checkResult(Object result, Class<?> returnType, String expected) {
        return returnType.cast(result).toString().equals(expected);
    }

    private static boolean checkLengths(String[][] params, Class<?>[] parameterTypes) {
        int parameterTypesLength = parameterTypes.length;
        for (String[] paramList : params) {
            if (paramList.length != parameterTypesLength) {
                return false;
            }
        }
        return true;
    }

    private static Object[] convertParams(String[] params, Class<?>[] classes) {
        Object[] result = new Object[params.length];
        for (int i = 0; i < params.length; i++) {
            result[i] = convertParameter(params[i], classes[i]);
        }
        return result;
    }

    private static Object convertParameter(String param, Class<?> targetType) {
        if (targetType == String.class) {
            return param;
        }
        else if (targetType == Boolean.class) {
            return Boolean.parseBoolean(param);
        }
        else if (targetType == Byte.class) {
            return Byte.parseByte(param);
        }
        else if (targetType == Character.class) {
            if (param.length() != 1) {
                throw new IllegalArgumentException("Cannot convert string '" + param + "' to char, must be single character");
            }
            return param.charAt(0);
        }
        else if (targetType == Double.class) {
            return Double.parseDouble(param);
        }
        else if (targetType == Float.class) {
            return Float.parseFloat(param);
        }
        else if (targetType == Integer.class) {
            return Integer.parseInt(param);
        }
        else if (targetType == Long.class) {
            return Long.parseLong(param);
        }
        else if (targetType == Short.class) {
            return Short.parseShort(param);
        }
        throw new IllegalArgumentException("Unsupported parameter type: " + targetType);
    }
}
