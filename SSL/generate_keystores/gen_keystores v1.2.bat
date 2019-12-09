@echo off

:: Author: Joshua Fehrenbach
:: Version 1.2

:: VERSION STILL NOT COMPLETE

:: ensure Java's keytool utility (usually packaged with the JDK) is installed and configured
:: on the PATH before doing anything else
keytool 2>nul || (
  echo Could not find Java's keytool utility
  echo Please ensure the JDK bin directory is configured in the PATH environment variable
  exit /b 1
)
openssl version >nul || (
  echo Please ensure that OpenSSL is installed and configured
  echo in the PATH environment variable
  exit /b 2
)

setlocal EnableDelayedExpansion

:: define variables
set "_trustpass="
set "_keypass="
set "_srcdir="
set "_dstdir="
set "_keystore="
set "_truststore="
set "_ca_key="
set "_ca_cert="
set "_server_key="
set "_server_cert="
set "_client_key="
set "_client_cert="
set "_web_key="
set "_web_cert="
:: skip function definitions
goto start

:: =========== BEGIN ARGUMENT CHECK FUNCTION DEFINITIONS ===========

:badopt
:: invalid option
echo Unrecognized command option: !arg! >&2
echo(>&2
call :usage 3
exit /b

:check-srcdir-set
if not [!_srcdir!]==[] (
  echo Input directory can only be defined once >&2
  echo(>&2
  call :usage 4
)
exit /b

:check-dstdir-set
if not [!_dstdir!]==[] (
  echo Output directory can only be defined once >&2
  echo(>&2
  call :usage 5
)
exit /b

:check-truststore-pass-set
if not [!_trustpass!]==[] (
  echo Truststore password can only be defined once >&2
  echo(>&2
  call :usage 6
)
exit /b

:check-keystore-pass-set
if not [!_keypass!]==[] (
  echo Keystore password can only be defined once >&2
  echo(>&2
  call :usage 7
)
exit /b

:check-truststore-file-set
if not [!_truststore!]==[] (
  echo Truststore file can only be defined once >&2
  echo(>&2
  call :usage 8
)
exit /b

:check-keystore-file-set
if not [!_keystore!]==[] (
  echo Keystore file can only be defined once >&2
  echo(>&2
  call :usage 9
)
exit /b

:check-ca-key-file-set
if not [!_ca_key!]==[] (
  echo CA Private Key file can only be defined once >&2
  echo(>&2
  call :usage 10
)
exit /b

:check-ca-cert-file-set
if not [!_ca_cert!]==[] (
  echo CA Certificate file can only be defined once >&2
  echo(>&2
  call :usage 11
)
exit /b

:check-server-key-file-set
if not [!_server_key!]==[] (
  echo MySQL Server Private Key file can only be defined once >&2
  echo(>&2
  call :usage 12
)
exit /b

:check-server-cert-file-set
if not [!_server_cert!]==[] (
  echo MySQL Server Certificate file can only be defined once >&2
  echo(>&2
  call :usage 13
)
exit /b

:check-client-key-file-set
if not [!_client_key!]==[] (
  echo MySQL Client Private Key file can only be defined once >&2
  echo(>&2
  call :usage 14
)
exit /b

:check-client-cert-file-set
if not [!_client_cert!]==[] (
  echo MySQL Client Certificate file can only be defined once >&2
  echo(>&2
  call :usage 15
)
exit /b

:check-web-key-file-set
if not [!_web_key!]==[] (
  echo Tomcat Web Server Private Key file can only be defined once >&2
  echo(>&2
  call :usage 16
)
exit /b

:check-web-cert-file-set
if not [!_web_cert!]==[] (
  echo Tomcat Web Server Certificate file can only be defined once >&2
  echo(>&2
  call :usage 17
)
exit /b

:: =========== END ARGUMENT CHECK FUNCTION DEFINITIONS ===========

:: ========== BEGIN UTILITY FUNCTION DEFINITIONS ==========

:NORMALIZEOUTPATH
:: ensure that, if the file exists, then it is also a directory and
:: that, if it does not exist, then it is created
:: check if the path exists as a directory
if exist "%~f1\" (
  set "retval=%~f1"
  exit /b 0
)
:: ensure that, since the path is not a directory, the path is not an existing file
if exist "%~f1" (
  set "retval="
  exit /b 1
)
:: path does not exist, so try to create it
mkdir "%~f1" || (
  set "retval="
  exit /b 2
)
:: directory successfully created
set "retval=%~f1"
exit /b 0


:NORMALIZEINPATH
:: ensure that the file exists and that it is a directory
:: check if the path exists as a directory
if exist "%~f1\" (
  set "retval=%~f1"
  exit /b 0
)
set "retval="
:: path does not exist as a directory, so check if the path exists at all
if not exist "%~f1" (
  exit /b 1
)
:: path exists as a file
exit /b 2


:NORMALIZEOUTFILE
:: ensure that, if the file exists, then it is also not a directory
:: and that, if the directory it is in does not exist, then that
:: directory is created
if exist "%~f1\" (
  set "retval="
  call :BADFILE "%~f1" 18
  exit /b
)
if exist "%~f1" (
  set "retval=%~1"
  exit /b 0
)
call :NORMALIZEOUTPATH "%~d1" || (
  set /A "_err=!ERRORLEVEL!+19"
  call :BADFILE "%~f1" !_err!
  exit /b
)
set "retval=%~1"
exit /b 0


:NORMALIZEINFILE
:: ensure that the file exists and that it is not a directory
if exist "%~f1\" (
  set "retval="
  call :BADFILE "%~f1" 18
  exit /b
)
if not exist "%~f1" (
  set "retval="
  call :BADFILE "%~f1" 19
  exit /b
)
set "retval=%~1"
exit /b 0


:GET_CHECK_SRCDIR
:: ensure that the file exists and that it is a directory
call :NORMALIZEINPATH "%~1" || (
  set /A "_err=!ERRORLEVEL!+21"
  call :BADDIR "%~1" !_err!
  exit /b
)
exit /b 0


:GET_CHECK_DSTDIR
call :NORMALIZEOUTPATH "%~1" || (
  set /A "_err=!ERRORLEVEL!+22"
  call :BADDIR "%~1" !_err!
  exit /b
)
exit /b 0


:STRLEN
:: by Dave Benham and others from the DosTips forum (:strlen6)
:: ss64.org/viewtopic.php?pid=6478#p6478
set "strlen_s=#%~1"
set "strlen_len=0"
for /L %%A in (12,-1,0) do (
  set /A "strlen_len|=1<<%%A"
  for %%B in (!strlen_len!) do if "!strlen_s:~%%B,1!"=="" set /A "strlen_len&=~1<<%%A"
)
set "retval=!strlen_len!"
exit /b


:CHECK_TRUSTPASS
call :STRLEN "%~1"
if !retval! LSS 6 call :BADTRUSTPASS "!retval!"
exit /b


:CHECK_KEYPASS
call :STRLEN "%~1"
if !retval! LSS 6 call :BADKEYPASS "!retval!"
exit /b

:: =========== END UTILITY FUNCTION DEFINITIONS ===========

:: =========== BEGIN ERROR FUNCTION DEFINITIONS ===========

:BADFILE
echo Could not open "%~1" as a file >&2
if %~2 EQU 18 (
  echo File is a directory >&2
) else if %~2 EQU 19 (
  echo Input file does not exist >&2
) else if %~2 EQU 20 (
  echo Root directory is a file >&2
) else if %~2 EQU 21 (
  echo Root directory does not exist and could not be created >&2
) else (
  echo Unknown Reason >&2
  echo Error code: %~2 >&2
)
echo(>&2
call :usage %~2
exit /b

:BADDIR
echo Could not open "%~1" as a directory >&2
if %~2 EQU 22 (
  echo Input directory does not exist >&2
) if %~2 EQU 23 (
  echo Directory is a file >&2
) else if %~2 EQU 24 (
  echo Directory does not exist and could not be created >&2
) else else (
  echo Unknown Reason >&2
  echo Error code: %~2 >&2
)
echo(>&2
call :usage %~2
exit /b

:BADTRUSTPASS
echo Invalid truststore password length: %~1 >&2
echo Must be at least 6 characters in length >&2
echo(>&2
call :usage 25
exit /b

:BADKEYPASS
echo Invalid keystore password length: %~1 >&2
echo Must be at least 6 characters in length >&2
echo(>&2
call :usage 26
exit /b

:: print the usage information and return the set error number
:usage
echo Small Town Ships Truststore and Keystore Generation Script
echo:
echo Commands
echo:
echo -srcdir directory       Defines the default directory in which to search for input files. Defaults
echo                           to the directory in which this script was run.
echo -dstdir directory       Defines the default directory in which the generated files are stored.
echo -truststore-pass pass   Defines the password to use for the generated truststore. Must be at least
echo                           six characters long. Defaults to the password set to use for the
echo                           keystore. A password must be provided for either the truststore or the
echo                           keystore, or both.
echo -keystore-pass pass     Defines the password to use for the generated keystore. Must be at least
echo                           six characters long. Defaults to the password set to use for the
echo                           truststore. A password must be provided for either the truststore or the
echo                           keystore, or both.
echo:
echo                           Defaults to the directory in which this script was run.
echo -truststore-file file   Defines the file path for the generated truststore file. This option
echo                           overrides -dstdir for the truststore, and the specified file will be
echo                           modified if it already exists.
echo                           Default is %%DSTDIR%%\truststore
echo -keystore-file file     Defines the file path for the generated keystore file. This option
echo                           overrides -dstdir for the keystore, and the specified file will be
echo                           modified if it already exists.
echo                           Default is %%DSTDIR%%\keystore
echo:
echo -ca-key-file file       Defines the file path of the CA private key. This option overrides -srcdir
echo                           for the CA private key. Defaults to %%SRCDIR%%\ca-key.pem
echo -ca-cert-file file      Defines the file path of the CA self-signed certificate. This option
echo                           overrides -srcdir for the CA certificate.
echo                           Defaults to %%SRCDIR%%\ca.pem
echo -server-key-file file   Defines the file path of the MySQL Server private key. This option
echo                           overrides -srcdir for the MySQL Server private key.
echo                           Defaults to %%SRCDIR%%\server-key.pem
echo -server-cert-file file  Defines the file path of the MySQL Server certificate. This option
echo                           overrides -srcdir for the MySQL Server certificate.
echo                           Defaults to %%SRCDIR%%\server-cert.pem
echo -client-key-file file   Defines the file path of the MySQL Client private key. This option
echo                           overrides -srcdir for the MySQL Client private key.
echo                           Defaults to %%SRCDIR%%\client-key.pem
echo -client-cert-file file  Defines the file path of the MySQL Client certificate. This option
echo                           overrides -srcdir for the MySQL Client certificate.
echo                           Defaults to %%SRCDIR%%\client-cert.pem
echo -web-key-file file      Defines the file path of the Tomcat Web Server private key. This option
echo                           overrides -srcdir for the Tomcat Web Server private key.
echo                           Defaults to %%SRCDIR%%\web-key.pem
echo -web-cert-file file     Defines the file path of the Tomcat Web Server certificate. This option
echo                           overrides -srcdir for the Tomcat Web Server certificate.
echo                           Defaults to %%SRCDIR%%\web-cert.pem
exit /b %~1


:: begin script
:start

:: count the arguments and store them in the vector 'argv'
set "argc=0"
for %%G in (%*) do (
  set /A "argc+=1"
  set "argv[!argc!]=%%~G"
)

:: ========== begin parsing command line arguments ==========

set "last="
:: skip the loop if we have no command-line arguments
if !argc! LEQ 0 goto afterloop
set /A G=1
:: use pseudo for-loop since batch for loops are funny about goto statements
:loopstart

set arg=!argv[%G%]!

:: check whether we need to get an argument's parameter
if not [!last!]==[] goto get!last!

:: no parameter to get, so parse the next argument

:: check for help flag
set or=0
if /I !arg!==-help set or=1
if /I !arg!==-h set or=1
if /I !arg!==-? set or=1
if /I !arg!==/h set or=1
if /I !arg!==/? set or=1

:: print help message and exit
if !or! EQU 1 (
  call :usage 0
  exit /b 0
)

:: next argument is input directory
if /I !arg!==-srcdir goto goodopt
:: next argument is output directory
if /I !arg!==-dstdir goto goodopt
:: next argument is truststore password
if /I !arg!==-truststore-pass goto goodopt
:: next argument is keystore password
if /I !arg!==-keystore-pass goto goodopt
:: next argument is truststore file
if /I !arg!==-truststore-file goto goodopt
:: next argument is keystore file
if /I !arg!==-keystore-file goto goodopt
:: next argument is CA private key
if /I !arg!==-ca-key-file goto goodopt
:: next argument is CA certificate
if /I !arg!==-ca-cert-file goto goodopt
:: next argument is MySQL server private key
if /I !arg!==-server-key-file goto goodopt
:: next argument is MySQL server certificate
if /I !arg!==-server-cert-file goto goodopt
:: next argument is MySQL client private key
if /I !arg!==-client-key-file goto goodopt
:: next argument is MySQL client certificate
if /I !arg!==-client-cert-file goto goodopt
:: next argument is Tomcat web server private key
if /I !arg!==-web-key-file goto goodopt
:: next argument is Tomcat web server certificate
if /I !arg!==-web-cert-file goto goodopt

goto badopt

:goodopt
:: ensure option not already used
call :check!arg!-set || exit /b
:: tell the next iteration to parse the option's argument
set "last=!arg!"
goto loopend


:: ========== begin parsing parameter arguments ==========

:get-srcdir
:: parse input directory
call :GET_CHECK_SRCDIR "!arg!" || exit /b
set "_srcdir=!retval!\"
goto parseend

:get-dstdir
:: parse output directory
call :GET_CHECK_DSTDIR "!arg!" || exit /b
set "_dstdir=!retval!\"
goto parseend

:get-truststore-pass
:: parse truststore password
call :CHECK_TRUSTPASS "!arg!" || exit /b
set "_trustpass=!arg!"
goto parseend

:get-keystore-pass
:: parse keystore password
call :CHECK_KEYPASS "!arg!" || exit /b
set "_keypass=!arg!"
goto parseend

:get-truststore-file
:: parse truststore file
call :NORMALIZEOUTFILE "!arg!" || exit /b
set _truststore="!retval!"
goto parseend

:get-keystore-file
:: parse keystore file
call :NORMALIZEOUTFILE "!arg!" || exit /b
set _keystore="!retval!"
goto parseend

:get-ca-key-file
:: parse CA private key file
call :NORMALIZEINFILE "!arg!" || exit /b
set _ca_key="!retval!"
goto parseend

:get-ca-cert-file
:: parse CA certificate file
call :NORMALIZEINFILE "!arg!" || exit /b
set _ca_cert="!retval!"
goto parseend

:get-server-key-file
:: parse MySQL server private key file
call :NORMALIZEINFILE "!arg!" || exit /b
set _server_key="!retval!"
goto parseend

:get-server-cert-file
:: parse MySQL server certificate file
call :NORMALIZEINFILE "!arg!" || exit /b
set _server_cert="!retval!"
goto parseend

:get-client-key-file
:: parse MySQL client private key file
call :NORMALIZEINFILE "!arg!" || exit /b
set _client_key="!retval!"
goto parseend

:get-client-cert-file
:: parse MySQL client certificate file
call :NORMALIZEINFILE "!arg!" || exit /b
set _client_cert="!retval!"
goto parseend

:get-web-key-file
:: parse Tomcat web server private key file
call :NORMALIZEINFILE "!arg!" || exit /b
set _web_key="!retval!"
goto parseend

:get-web-cert-file
:: parse Tomcat web server certificate file
call :NORMALIZEINFILE "!arg!" || exit /b
set _web_cert="!retval!"

:: ========== end parsing parameter arguments ==========

:parseend
set "last="

:loopend
set /A "G+=1"
if !G! LEQ !argc! goto loopstart

:: ========== end parsing command line arguments ==========

:afterloop
:: ensure that there were no dangling options
if not [%last%]==[] (
  echo Option "%last%" requires an argument >&2
  echo(>&2
  call :usage 27
  exit /b
)


:: ========== set default values where necessary ==========

if [%_trustpass%]==[] if [%_keypass%]==[] (
  echo Must provide a password for the truststore, the keystore, or both. >&2
  echo(>&2
  call :usage 28
  exit /b
)
if [!_trustpass!]==[] (
  set "_trustpass=!_keypass!"
) else if [!_keypass!]==[] (
  set "_keypass=!_trustpass!"
)

if [!_dstdir!]==[] (
  call :GET_CHECK_DSTDIR "." || exit /b
  set "_dstdir="
)
if [!_srcdir!]==[] (
  call :GET_CHECK_SRCDIR "." || exit /b
  set "_srcdir="
)

if [!_truststore!]==[] (
  call :NORMALIZEOUTFILE "%_dstdir%truststore" || exit /b
  set _truststore="!retval!"
)
if [!_keystore!]==[] (
  call :NORMALIZEOUTFILE "%_dstdir%keystore" || exit /b
  set _keystore="!retval!"
)

if [!_ca_key!]==[] (
  call :NORMALIZEINFILE "%_srcdir%ca-key.pem" || exit /b
  set _ca_key="!retval!"
)
if [!_ca_cert!]==[] (
  call :NORMALIZEINFILE "%_srcdir%ca.pem" || exit /b
  set _ca_cert="!retval!"
)

if [!_server_key!]==[] (
  call :NORMALIZEINFILE "%_srcdir%server-key.pem" || exit /b
  set _server_key="!retval!"
)
if [!_server_cert!]==[] (
  call :NORMALIZEINFILE "%_srcdir%server-cert.pem" || exit /b
  set _server_cert="!retval!"
)

if [!_client_key!]==[] (
  call :NORMALIZEINFILE "%_srcdir%client-key.pem" || exit /b
  set _client_key="!retval!"
)
if [!_client_cert!]==[] (
  call :NORMALIZEINFILE "%_srcdir%client-cert.pem" || exit /b
  set _client_cert="!retval!"
)

if [!_web_key!]==[] (
  call :NORMALIZEINFILE "%_srcdir%web-key.pem" || exit /b
  set _web_key="!retval!"
)
if [!_web_cert!]==[] (
  call :NORMALIZEINFILE "%_srcdir%web-cert.pem" || exit /b
  set _web_cert="!retval!"
)

:: ========== generate truststore and keystore ==========

set _tmp_keystore="%TEMP%\smalltownships-tmp-keystore%RANDOM%.p12"

echo Generating Truststore
if exist %_truststore% (
  keytool -delete -alias root -keystore %_truststore% -storepass %_trustpass% >nul 2>&1
  keytool -delete -alias mysql_server -keystore %_truststore% -storepass %_trustpass% >nul 2>&1
  keytool -delete -alias mysql_client -keystore %_truststore% -storepass %_trustpass% >nul 2>&1
  keytool -delete -alias tomcat -keystore %_truststore% -storepass %_trustpass% >nul 2>&1
)
keytool -importcert -noprompt -alias root -file %_ca_cert% -keystore %_truststore% -storepass %_trustpass% >nul 2>&1 || (
  echo Could not generate truststore or add CA certificate to truststore >&2
  exit /b 29
)
keytool -importcert -noprompt -alias mysql_server -file %_server_cert% -keystore %_truststore% -storepass %_trustpass% -trustcacerts >nul 2>&1 || (
  echo Could not add MySQL server certificate to truststore >&2
  exit /b 30
)
keytool -importcert -noprompt -alias mysql_client -file %_client_cert% -keystore %_truststore% -storepass %_trustpass% -trustcacerts >nul 2>&1 || (
  echo Could not add MySQL client certificate to truststore >&2
  exit /b 31
)
keytool -importcert -noprompt -alias tomcat -file %_web_cert% -keystore %_truststore% -storepass %_trustpass% -trustcacerts >nul 2>&1 || (
  echo Could not add Tomcat web server certificate to truststore >&2
  exit /b 32
)

echo Generating Keystore
if exist %_keystore% (
  keytool -delete -alias mysql_client -keystore %_keystore% -storepass %_keypass% >nul 2>&1
  keytool -delete -alias tomcat -keystore %_keystore% -storepass %_keypass% >nul 2>&1
)
openssl pkcs12 -export -in %_client_cert% -inkey %_client_key% -name mysql_client -passout pass:%_keypass% -out %_tmp_keystore% >nul || (
  echo Could not generate PKCS#12 archive for the MySQL client certificate and private key >&2
  exit /b 33
)
keytool -importkeystore -noprompt -srckeystore %_tmp_keystore% -srcstoretype pkcs12 -srcstorepass %_keypass% -destkeystore %_keystore% -deststoretype jks -deststorepass %_keypass% >nul 2>&1 || (
  echo Could not generate keystore or add MySQL client certificate and key to keystore >&2
  del %_tmp_keystore%
  exit /b 34
)
del %_tmp_keystore%
openssl pkcs12 -export -in %_web_cert% -inkey %_web_key% -name tomcat -passout pass:%_keypass% -out %_tmp_keystore% >nul || (
  echo Could not generate PKCS#12 archive for the Tomcat web server certificate and private key >&2
  exit /b 35
)
keytool -importkeystore -noprompt -srckeystore %_tmp_keystore% -srcstoretype pkcs12 -srcstorepass %_keypass% -destkeystore %_keystore% -deststoretype jks -deststorepass %_keypass% >nul 2>&1 || (
  echo Could not add Tomcat web server certificate and key to keystore >&2
  del %_tmp_keystore%
  exit /b 36
)
del %_tmp_keystore%

echo:
echo Generation complete

endlocal

@echo on