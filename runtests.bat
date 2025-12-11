@echo off
if "%1"=="" (
    echo Usage: runtests fully.qualified.ClassName
    exit /b 1
)

echo Compiling...
javac src/main/java/uj/wmii/pwj/anns/*.java

if errorlevel 1 (
    echo Compilation failed!
    del /Q src\main\java\uj\wmii\pwj\anns\*.class 2>nul
    exit /b 1
)

echo Running tests...
java -cp src/main/java uj.wmii.pwj.anns.MyTestEngine %1

echo Cleaning up...
del /Q src\main\java\uj\wmii\pwj\anns\*.class