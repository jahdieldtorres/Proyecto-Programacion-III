@echo off
REM ---------------------------------------------------------------
REM Arma la carpeta ORIGEN (con ORIGEN.exe y su propio Java) usando jpackage.
REM
REM Antes de correrlo:
REM   1. Todo el nucleo (A1 a A3, J1 a J3, H1 y H2) fusionado en develop.
REM   2. JDK 21 activo (java -version debe decir 21) y mvn en el PATH.
REM   3. El wallet en src\main\resources\wallet\ y db.properties en
REM      src\main\resources\ (A3). NO se suben a GitHub.
REM
REM Resultado: target\dist\ORIGEN\ORIGEN.exe. Esa carpeta NO se sube a GitHub;
REM se comparte por Drive, solo con el grupo.
REM ---------------------------------------------------------------
setlocal

if not exist src\main\resources\wallet\tnsnames.ora (
    echo AVISO: no hay wallet en src\main\resources\wallet. El .exe no podra conectarse sin el.
    pause
)

call mvn clean package || goto :error
call mvn dependency:copy-dependencies -DoutputDirectory=target/libs -DincludeScope=runtime || goto :error
copy target\autoparts-portal-0.1-SNAPSHOT.jar target\libs\ || goto :error

jpackage --type app-image --name ORIGEN --input target\libs ^
    --main-jar autoparts-portal-0.1-SNAPSHOT.jar --main-class autoparts.Launcher ^
    --dest target\dist || goto :error

REM Las fotos van junto a ORIGEN.exe (la app las busca en la carpeta images)
xcopy images target\dist\ORIGEN\images\ /E /I /Y || goto :error

echo.
echo Listo: target\dist\ORIGEN\ORIGEN.exe
exit /b 0

:error
echo.
echo Algo fallo. Revisa el mensaje de arriba.
exit /b 1
