@echo off
setlocal enabledelayedexpansion
REM ================================================================
REM  IronGate Vault — Build Fat JAR (Step 1 before making .exe)
REM  Run this BEFORE running launch4j to create IronGateVault.exe
REM ================================================================

set JAVAFX_PATH=C:\Users\Muhammad Irtaza\OneDrive\Documents\javafx-sdk-25.0.3\lib
set JAVA_HOME=C:\Program Files\Java\jdk-21

set SRC=src
set OUT=out
set LIB=lib
set JAR_NAME=IronGateVault-all.jar

echo ============================================================
echo   IronGate Vault — Build Fat JAR
echo ============================================================

if not exist "%OUT%" mkdir "%OUT%"
if not exist "staging" mkdir staging

REM ── Step 1: Compile ─────────────────────────────────────────
echo [1/4] Compiling sources...

"%JAVA_HOME%\bin\javac" ^
  --module-path "%JAVAFX_PATH%" ^
  --add-modules javafx.controls,javafx.fxml,javafx.swing ^
  -cp "%LIB%\*" ^
  -d "%OUT%" ^
  "%SRC%\com\irongate\util\DBConnection.java" ^
  "%SRC%\com\irongate\util\SessionManager.java" ^
  "%SRC%\com\irongate\model\User.java" ^
  "%SRC%\com\irongate\model\VaultFile.java" ^
  "%SRC%\com\irongate\model\ActivityLog.java" ^
  "%SRC%\com\irongate\security\BCryptUtil.java" ^
  "%SRC%\com\irongate\security\AESUtil.java" ^
  "%SRC%\com\irongate\security\HashUtil.java" ^
  "%SRC%\com\irongate\security\OTPService.java" ^
  "%SRC%\com\irongate\dao\UserDAO.java" ^
  "%SRC%\com\irongate\dao\FileDAO.java" ^
  "%SRC%\com\irongate\dao\ActivityLogDAO.java" ^
  "%SRC%\com\irongate\service\AuthService.java" ^
  "%SRC%\com\irongate\service\FileService.java" ^
  "%SRC%\com\irongate\service\PasswordCrackerService.java" ^
  "%SRC%\com\irongate\ui\StyleUtil.java" ^
  "%SRC%\com\irongate\ui\MainApp.java" ^
  "%SRC%\com\irongate\ui\LoginScreen.java" ^
  "%SRC%\com\irongate\ui\RegisterScreen.java" ^
  "%SRC%\com\irongate\ui\OTPScreen.java" ^
  "%SRC%\com\irongate\ui\DashboardScreen.java" ^
  "%SRC%\com\irongate\ui\MyFilesPanel.java" ^
  "%SRC%\com\irongate\ui\DuplicatesPanel.java" ^
  "%SRC%\com\irongate\ui\ActivityLogPanel.java" ^
  "%SRC%\com\irongate\ui\SecurityPanel.java" ^
  "%SRC%\com\irongate\ui\ProfilePanel.java" ^
  "%SRC%\com\irongate\ui\PasswordCrackerPanel.java"

if errorlevel 1 ( echo [ERROR] Compile failed. & pause & exit /b 1 )

REM ── Step 2: Extract all dependency JARs into staging ────────
echo [2/4] Extracting dependency JARs...
cd staging
for %%f in (..\%LIB%\*.jar) do (
    "%JAVA_HOME%\bin\jar" xf "%%f"
)
cd ..

REM ── Step 3: Copy compiled classes into staging ───────────────
echo [3/4] Merging compiled classes...
xcopy /s /q "%OUT%\*" "staging\" >nul

REM Copy logo and vault_storage placeholder
copy /Y logo.png staging\logo.png >nul 2>&1

REM ── Step 4: Create manifest and pack JAR ────────────────────
echo [4/4] Creating %JAR_NAME%...
(
echo Manifest-Version: 1.0
echo Main-Class: com.irongate.ui.MainApp
echo.
) > staging\MANIFEST.MF

"%JAVA_HOME%\bin\jar" cfm "%JAR_NAME%" staging\MANIFEST.MF -C staging .

if errorlevel 1 ( echo [ERROR] JAR creation failed. & pause & exit /b 1 )

echo.
echo ============================================================
echo   SUCCESS: %JAR_NAME% created!
echo   Now open Launch4j and load launch4j_config.xml
echo   to generate IronGateVault.exe
echo ============================================================
echo.

REM Clean up staging
rmdir /s /q staging

pause
