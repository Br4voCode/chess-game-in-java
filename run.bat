@echo off
cd /d "%~dp0"

REM Crear directorio target\classes si no existe
if not exist "target\classes" mkdir "target\classes"

REM Definir rutas de JavaFX
set JAVAFX_PATH=%USERPROFILE%\.m2\repository\org\openjfx
set JAVAFX_CONTROLS=%JAVAFX_PATH%\javafx-controls\17.0.8\javafx-controls-17.0.8-win.jar
set JAVAFX_BASE=%JAVAFX_PATH%\javafx-base\17.0.8\javafx-base-17.0.8-win.jar
set JAVAFX_GRAPHICS=%JAVAFX_PATH%\javafx-graphics\17.0.8\javafx-graphics-17.0.8-win.jar
set JAVAFX_FXML=%JAVAFX_PATH%\javafx-fxml\17.0.8\javafx-fxml-17.0.8-win.jar

set CLASSPATH=%JAVAFX_CONTROLS%;%JAVAFX_BASE%;%JAVAFX_GRAPHICS%;%JAVAFX_FXML%

REM Compilar el proyecto
echo Compilando el proyecto...
for /r "src\main\java" %%f in (*.java) do (
    javac -cp "%CLASSPATH%;target\classes" -d target\classes "%%f"
)

if %ERRORLEVEL% neq 0 (
    echo Error en la compilacion
    pause
    exit /b 1
)

REM Copiar recursos
if exist "src\main\resources" xcopy "src\main\resources" "target\classes" /E /I /Y

REM Ejecutar la aplicacion con JavaFX
echo Ejecutando la aplicacion...
java --module-path "%JAVAFX_CONTROLS%;%JAVAFX_BASE%;%JAVAFX_GRAPHICS%;%JAVAFX_FXML%" --add-modules javafx.controls,javafx.fxml -cp "target\classes" chess.AppLauncher

pause