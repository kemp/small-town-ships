@echo off

:: Author: Joshua Fehrenbach
:: Version 1.2.4

:: ensure OpenSSL is installed and configured on the PATH before doing anything else
openssl version >nul || (
  echo Please ensure that OpenSSL is installed and configured
  echo in the PATH environment variable
  exit /b 1
)

setlocal EnableDelayedExpansion

:: define variables
set "openssl="
set "_alg="
set "_ver="
set "_dstdir="
set "_ca_key="
set "_ca_cert="
set "_ca_alg="
set "_ca_ver="
set "_server_key="
set "_server_cert="
set "_server_alg="
set "_server_ver="
set "_client_key="
set "_client_cert="
set "_client_alg="
set "_client_ver="
set "_web_key="
set "_web_cert="
set "_web_alg="
set "_web_ver="
set "_priv_key="
set "_pub_key="
set "_priv_ver="
:: skip function definitions
goto start

:: cannot have nested block statements such as
::
::   if /I !arg!==-alg (
::     if not [!_alg!]==[] (
::       echo Algorithm can only be defined once >&2
::       echo(>&2
::       set _err=2
::       goto usage
::     )
::   ) else if ...
::
:: The workaround is to have the nested blocks defined as subroutines,
:: which is a pain and horribly inefficient but unfortunately must be done

:: =========== BEGIN ARGUMENT CHECK FUNCTION DEFINITIONS ===========

:badopt
:: invalid option
echo Unrecognized command option: !arg! >&2
echo(>&2
call :usage 2
exit /b

:check-alg-set
if not [!_alg!]==[] (
  echo Algorithm can only be defined once >&2
  echo(>&2
  call :usage 3
)
exit /b

:check-alg-ver-set
if not [!_ver!]==[] (
  echo Algorithm version can only be defined once >&2
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

:check-ca-key-file-set
if not [!_ca_key!]==[] (
  echo CA private key file can only be defined once >&2
  echo(>&2
  call :usage 6
)
exit /b

:check-ca-cert-file-set
if not [!_ca_cert!]==[] (
  echo CA self-signed certificate file can only be defined once >&2
  echo(>&2
  call :usage 7
)
exit /b

:check-ca-alg-set
if not [!_ca_alg!]==[] (
  echo CA private key and certificate algorithm can only be defined once >&2
  echo(>&2
  call :usage 8
)
exit /b

:check-ca-alg-ver-set
if not [!_ca_ver!]==[] (
  echo CA private key and certificate algorithm version can only be defined once >&2
  echo(>&2
  call :usage 9
)
exit /b

:check-server-key-file-set
if not [!_server_key!]==[] (
  echo MySQL server private key file can only be defined once >&2
  echo(>&2
  call :usage 10
)
exit /b

:check-server-cert-file-set
if not [!_server_cert!]==[] (
  echo MySQL server certificate file can only be defined once >&2
  echo(>&2
  call :usage 11
)
exit /b

:check-server-alg-set
if not [!_server_alg!]==[] (
  echo MySQL server private key and certificate algorithm can only be defined once >&2
  echo(>&2
  call :usage 12
)
exit /b

:check-server-alg-ver-set
if not [!_server_ver!]==[] (
  echo MySQL server private key and certificate algorithm version can only be defined once >&2
  echo(>&2
  call :usage 13
)
exit /b

:check-client-key-file-set
if not [!_client_key!]==[] (
  echo MySQL client private key file can only be defined once >&2
  echo(>&2
  call :usage 14
)
exit /b

:check-client-cert-file-set
if not [!_client_cert!]==[] (
  echo MySQL client certificate file can only be defined once >&2
  echo(>&2
  call :usage 15
)
exit /b

:check-client-alg-set
if not [!_client_alg!]==[] (
  echo MySQL client private key and certificate algorithm can only be defined once >&2
  echo(>&2
  call :usage 16
)
exit /b

:check-client-alg-ver-set
if not [!_client_ver!]==[] (
  echo MySQL client private key and certificate algorithm version can only be defined once >&2
  echo(>&2
  call :usage 17
)
exit /b

:check-web-key-file-set
if not [!_web_key!]==[] (
  echo Tomcat web server private key file can only be defined once >&2
  echo(>&2
  call :usage 18
)
exit /b

:check-web-cert-file-set
if not [!_web_cert!]==[] (
  echo Tomcat web server certificate file can only be defined once >&2
  echo(>&2
  call :usage 19
)
exit /b

:check-web-alg-set
if not [!_web_alg!]==[] (
  echo Tomcat web server private key and certificate algorithm can only be defined once >&2
  echo(>&2
  call :usage 20
)
exit /b

:check-web-alg-ver-set
if not [!_web_ver!]==[] (
  echo Tomcat web server private key and certificate algorithm version can only be defined once >&2
  echo(>&2
  call :usage 21
)
exit /b

:check-priv-key-file-set
if not [!_priv_key!]==[] (
  echo MySQL password authentication private key file can only be defined once >&2
  echo(>&2
  call :usage 22
)
exit /b

:check-pub-key-file-set
if not [!_pub_key!]==[] (
  echo MySQL password authentication public key file can only be defined once >&2
  echo(>&2
  call :usage 23
)
exit /b

:check-priv-key-ver-set
if not [!_priv_ver!]==[] (
  echo MySQL password authentication key pair RSA version can only be defined once >&2
  echo(>&2
  call :usage 24
)
exit /b

:check-openssl-path-set
if not [!openssl!]==[] (
  echo OpenSSL Executable can only be defined once >&2
  echo(>&2
  call :usage 47
)
exit /b

:: =========== END ARGUMENT CHECK FUNCTION DEFINITIONS ===========

:: ========== BEGIN UTILITY FUNCTION DEFINITIONS ==========

:NORMALIZEPATH
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
set "retval=%~1"
exit /b 0


:NORMALIZEFILE
:: ensure that, if the file exists, then it is also not a directory
:: and that, if the directory it is in does not exist, then that
:: directory is created
if exist "%~f1\" (
  set "retval="
  call :BADFILE "%~f1"
  exit /b
)
if exist "%~f1" (
  set "retval=%~1"
  exit /b 0
)
call :NORMALIZEPATH "%~d1" || (
  set "retval="
  call :BADROOT "%~f1" "%~d1"
  exit /b
)
set "retval=%~1"
exit /b 0


:CHECKALG
:: ensure that the algorithm is valid and that it is in all lowercase
if /I %~1==rsa (
  set "retval=rsa"
  exit /b 0
)
if /I %~1==dsa (
  set "retval=dsa"
  exit /b 0
)
if /I %~1==ecdsa (
  set "retval=ecdsa"
  exit /b 0
)
call :BADALG "%~1"
exit /b


:CHECKALGVER
:: ensure that the algorithm and version are compatible
:: if ((%alg == rsa || %alg == dsa) && (%ver < 512 || %ver > 24576)) exit /b 1
if /I %~1==ecdsa (
:: check for invalid curve name
  openssl ecparam -name %~2 -noout 2>nul || call :BADCURVE "%~2"
  exit /b
)
if /I not %~1==rsa if /I not %~1==dsa (
:: unrecognized algorithm
  call :BADALGVER "%~1" "%~2"
  exit /b
)

:: allow key size in the range [512, 24576] for RSA and DSA.
:: The maximum bit number allowed here is 24576 as the largest key
:: size included in NIST standards is 3072 bits for both RSA and DSA,
:: and the key size for providing comparable security to 256-bit AES
:: is 15360 bits (though it is not included in the NIST standard
:: for "interoperability and efficiency reasons"). Therefore, any
:: key size greater than 15360 bits is excessive.
:: See "NIST Special Publication 800-57 Part 1 Revision 4, Recommendation
:: for Key Management Part 1: General". Table 2 is a table of comparable
:: security strengths relative to AES and TDEA (also called DES-EDE).
if %~2 GEQ 512 if %~2 LEQ 24576 exit /b 0
:: invalid key size
call :BADALGVER "%~1" "%~2"
exit /b


:ALGFROMVER
:: attempt to get the algorithm from the version
:: vesion is a named curve, so the algorithm is ECDSA
openssl ecparam -name %~1 -noout 2>nul && (
  set "retval=ecdsa"
  exit /b 0
)
:: 512 <= version <= 24576, so algorithm is either RSA or DSA.
:: Defaults to RSA, even though both RSA and DSA use the key size
:: as their version
if %~1 GEQ 512 if %~1 LEQ 24576 (
  set "retval=rsa"
  exit /b 0
)
:: version does not apply to RSA, DSA, or ECDSA
call :BADVER "%~1"
exit /b


:VERFROMALG
:: get the default version according to the algorithm
if /I %~1==ecdsa (
  set "retval=secp256r1"
  exit /b 0
)
if /I not %~1==dsa if /I not %~1==rsa (
  call :BADALG "%~1"
  exit /b
)
set "retval=2048"
exit /b 0


:ALGVERDEFAULT
:: get the default algorithm and version
set "retalg=%~3"
set "retver=%~4"

if [!retalg!]==[] if [!retver!]==[] (
:: algorithm and version both not set, so use the default
  set "retalg=%~1"
  set "retver=%~2"
  exit /b 0
)
if [!retalg!]==[] if not [!retver!]==[] (
:: algorithm not set but version set
  call :ALGFROMVER "!retver!" || exit /b
  set "retalg=!retval!"
  exit /b 0
)
if not [!retalg!]==[] if [!retver!]==[] (
:: algorithm set but version not set
  call :VERFROMALG "!retalg!" || exit /b
  set "retver=!retval!"
  exit /b 0
)
:: algorithm and version both already set
call :CHECKALGVER "!retalg!" "!retver!"
exit /b


:GET_CHECK_DSTDIR
call :NORMALIZEPATH "%~1" || (
  call :BADDIR!ERRORLEVEL! "%~1"
)
exit /b

:CHECK_OPENSSL
call "%~f1" version >nul || (
  echo Could not find OpenSSL at "%~f1" >&2
  echo(>&2
  call :usage 48
)
exit /b

:: =========== END UTILITY FUNCTION DEFINITIONS ===========

:: =========== BEGIN ERROR FUNCTION DEFINITIONS ===========

:BADALG
echo Invalid algorithm name: %~1 >&2
echo(>&2
call :usage 25
exit /b


:BADFILE
echo Could not open "%~1" as a file >&2
echo "%~1" is a directory >&2
echo(>&2
call :usage 26
exit /b


:BADROOT
echo Could not open "%~1" as a file >&2
echo Root directory "%~2" could not be accessed or created >&2
echo(>&2
call :usage 27
exit /b


:BADDIR1
echo Could not open "%~1" as a directory >&2
echo "%~1" is a file >&2
echo(>&2
call :usage 28
exit /b


:BADDIR2
echo Could not open "%~1" as a directory >&2
echo Directory "%~1" could not be created >&2
echo(>&2
call :usage 29
exit /b


:BADCURVE
echo Named Elliptic Curve "%~1" is not supported by the OpenSSL installation >&2
echo(>&2
call :usage 30
exit /b


:BADALGVER
echo Algorithm "%~1" is not compatible with version "%~2" >&2
echo(>&2
call :usage 31
exit /b


:BADVER
echo Could not find an algorithm compatible with algorithm version "%~1" >&2
echo(>&2
call :usage 32
exit /b


:: =========== END ERROR FUNCTION DEFINITIONS ===========



:: print the usage information and return the set error number
:usage
echo Small Town Ships Key and Certificate Generation Script
echo:
echo Commands
echo:
echo -openssl-path exe-path     Defines the path to the OpenSSL executable. Defaults to searching the
echo                              PATH environment variable.
echo -alg algorithm             Defines the default algorithm to use to generate certificates and keys.
echo                              'algorithm' must be one of rsa, dsa, or ecdsa (default is rsa).
echo -alg-ver version           Defines the version of the default algorithm to use.
echo                              When using RSA, 'version' must be an integer ^>= 512 (default is 2048).
echo                              When using DSA, 'version' must be an integer ^>= 512 (default is 2048).
echo                              When using ECDSA, 'version' must be the name of a supported, named
echo                                Elliptic Curve (default is secp256r1). Note that some named curves
echo                                may not be supported by your OpenSSL installation. Use
echo                                "openssl ecparam -list_curves" to see a list of supported curves.
echo                                Note also that some supported curves may not be listed.
echo -dstdir directory          Defines the default output directory in which the generated files
echo                              are stored. Defaults to the directory in which this script was run.
echo -help                      Display this help message. All other options are ignored with this.
echo:
echo -ca-key-file file          Defines the file path for the generated CA private key.
echo                              This option overrides -dstdir for the CA private key, and the file
echo                              specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\ca-key.pem
echo -ca-cert-file file         Defines the file path for the generated CA self-signed certificate.
echo                              This option overrides -dstdir for the CA certificate, and the file
echo                              specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\ca.pem
echo -ca-alg algorithm          Defines the algorithm to use to generate the CA certificate and
echo                              private key. This option is the same as -alg, except it only sets
echo                              the algorithm to use for the CA private key and certificate.
echo -ca-alg-ver version        Defines the version of the algorithm to use to generate the CA
echo                              certificate and private key. This option is the same as -alg-ver,
echo                              except it only sets the algorithm version to use for the CA private
echo                              key and certificate.
echo:
echo -server-key-file file      Defines the file path for the generated MySQL server private key.
echo                              This option overrides -dstdir for the MySQL server private key, and
echo                              the file specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\server-key.pem
echo -server-cert-file file     Defines the file path for the generated MySQL server certificate.
echo                              This option overrides -dstdir for the MySQL server certificate, and
echo                              the file specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\server-cert.pem
echo -server-alg algorithm      Defines the algorithm to use to generate the MySQL server certificate
echo                              and private key. This option is the same as -alg, except it only
echo                              sets the algorithm to use for the MySQL server private key and
echo                              certificate.
echo -server-alg-ver version    Defines the version of the algorithm to use to generate the MySQL server
echo                              certificate and private key. This option is the same as -alg-ver,
echo                              except it only sets the algorithm version to use for the MySQL
echo                              server private key and certificate.
echo:
echo -client-key-file file      Defines the file path for the generated MySQL client private key.
echo                              This option overrides -dstdir for the MySQL client private key, and
echo                              the file specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\client-key.pem
echo -client-cert-file file     Defines the file path for the generated MySQL client certificate.
echo                              This option overrides -dstdir for the MySQL client certificate, and
echo                              the file specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\client-cert.pem
echo -client-alg algorithm      Defines the algorithm to use to generate the MySQL client certificate
echo                              and private key. This option is the same as -alg, except it only
echo                              sets the algorithm to use for the MySQL client private key and
echo                              certificate.
echo -client-alg-ver version    Defines the version of the algorithm to use to generate the MySQL client
echo                              certificate and private key. This option is the same as -alg-ver,
echo                              except it only sets the algorithm version to use for the MySQL client
echo                              private key and certificate.
echo:
echo -web-key-file file         Defines the file path for the generated Tomcat web server private key.
echo                              This option overrides -dstdir for the Tomcat web server private
echo                              key, and the file specified will be overwritten if it already exists.
echo                              Default is %%DSTDIR%%\web-key.pem
echo -web-cert-file file        Defines the file path for the generated Tomcat web server certificate.
echo                              This option overrides -dstdir for the Tomcat web server
echo                              certificate, and the file specified will be overwritten if it
echo                              already exists. Default is %%DSTDIR%%\web-cert.pem
echo -web-alg algorithm         Defines the algorithm to use to generate the Tomcat web server
echo                              certificate and private key. This option is the same as -alg,
echo                              except it only sets the algorithm to use for the Tomcat web server
echo                              private key and certificate.
echo -web-alg-ver version       Defines the version of the algorithm to use to generate the Tomcat web
echo                              server certificate and private key. This option is the same as
echo                              -alg-ver, except it only sets the algorithm version to use for the
echo                              Tomcat web server private key and certificate.
echo:
echo -priv-key-file file        Defines the file path for the MySQL password authentication private key.
echo                              This option overrides -dstdir for the MySQL password authentication
echo                              private key, and the file specified will be overwritten if it
echo                              already exists. Default is %%DSTDIR%%\private_key.pem
echo -pub-key-file file         Defines the file path for the MySQL password authentication public key.
echo                              This option overrides -dstdir for the MySQL password authentication
echo                              public key, and the file specified will be overwritten if it already
echo                              exists. Default is %%DSTDIR%%\public_key.pem
echo -priv-key-ver version      Defines the version of the algorithm to use to generate the MySQL
echo                              password authentication RSA key pair. This option is the same as
echo                              -alg-ver when using the option "-alg rsa", except it only sets the
echo                              RSA version to use for the MySQL password authentication key pair.
exit /b %~1




:: begin script
:start

:: count the arguments and store them in the vector 'argv'
set "argc=0"
for %%G in (%*) do (
  set /A "argc+=1"
  set "argv[!argc!]=%%~G"
)

:: batch if statements are extremely limited. There are no boolean AND or OR
:: operations. The workarounds are as follows:
::
:: boolean OR can be performed by using a temporary variable
::     set _tmp_or_var=0
::     if [cond1] set _tmp_or_var=1
::     if [cond2] set _tmp_or_var=1
::     if [cond3] set _tmp_or_var=1
::         ...
::     if [condN] set _tmp_or_var=1
::     if %_tmp_or_var% EQU 1 [command]
:: is equivalent to
::     if ([cond1] || [cond2] || [cond3] || ... || [condN]) [command]
:: which is quite cumbersome.

:: boolean AND can be performed by chaining if statements
::     if [cond1] if [cond2] if [cond3] ... if [condN] [command]
:: is equivalent to
::     if ([cond1] && [cond2] && [cond3] && ... && [condN]) [command]
:: however this does not allow if-else statements using boolean AND. This
:: can be achieved by using a temporary variable, similar to boolean OR
::     set _tmp_and_var=0
::     if [cond1] if [cond2] if [cond3] ... if [condN] set _tmp_and_var=1
::     if %_tmp_and_ver% EQU 1 ([command1]) else ([command2])
:: is equivalent to
::     if ([cond1] && [cond2] && [cond3] && ... && [condN])
::         [command1]
::     else
::         [command2]
::
:: These workarounds are already quite cumbersome, however they do not
:: account for combining boolean AND and OR operations, which would
:: require a messy combination of the above workarounds. For an example
:: of this, see the subroutine CHECKALGVER


:: ========== parse command line arguments ==========

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
if /I !arg!==-help call :usage 0 & exit /b
if /I !arg!==-h call :usage 0 & exit /b
if /I !arg!==-? call :usage 0 & exit /b
if /I !arg!==/h call :usage 0 & exit /b
if /I !arg!==/? call :usage 0 & exit /b

:: next argument is openssl path
if /I !arg!==-openssl-path goto goodopt
:: next argument is algorithm name
if /I !arg!==-alg goto goodopt
:: next argument is algorithm version
if /I !arg!==-alg-ver goto goodopt
:: next argument is output directory
if /I !arg!==-dstdir goto goodopt
:: next argument is CA private key file
if /I !arg!==-ca-key-file goto goodopt
:: next argument is CA certificate file
if /I !arg!==-ca-cert-file goto goodopt
:: next argument is CA algorithm name
if /I !arg!==-ca-alg goto goodopt
:: next argument is CA algorithm version
if /I !arg!==-ca-alg-ver goto goodopt
:: next argument is server private key file
if /I !arg!==-server-key-file goto goodopt
:: next argument is server certificate file
if /I !arg!==-server-cert-file goto goodopt
:: next argument is server algorithm name
if /I !arg!==-server-alg goto goodopt
:: next argument is server algorithm version
if /I !arg!==-server-alg-ver goto goodopt
:: next argument is client private key file
if /I !arg!==-client-key-file goto goodopt
:: next argument is client certificate file
if /I !arg!==-client-cert-file goto goodopt
:: next argument is client algorithm name
if /I !arg!==-client-alg goto goodopt
:: next argument is client algorithm version
if /I !arg!==-client-alg-ver goto goodopt
:: next argument is web server private key file
if /I !arg!==-web-key-file goto goodopt
:: next argument is web server certificate file
if /I !arg!==-web-cert-file goto goodopt
:: next argument is web server algorithm name
if /I !arg!==-web-alg goto goodopt
:: next argument is web server algorithm version
if /I !arg!==-web-alg-ver goto goodopt
:: next argument is password authentication private key
if /I !arg!==-priv-key-file goto goodopt
:: next argument is password authentication public key
if /I !arg!==-pub-key-file goto goodopt
:: next argument is password authentication RSA version
if /I !arg!==-priv-key-ver goto goodopt

goto badopt

:goodopt
:: ensure option not already used
call :check!arg!-set || exit /b
:: tell the next iteration to parse the option's argument
set "last=!arg!"
goto loopend


:: ========== parse parameter arguments ==========

:get-alg
:: parse algorithm name
call :CHECKALG "%arg%" || exit /b
set _alg=%arg%
:: ensure the algorithm name and version are compatible
if not [%_ver%]==[] (
  call :CHECKALGVER "%_alg%" "%_ver%" || exit /b
)
goto parseend

:get-alg-ver
:: parse algorithm version
set _ver=%arg%
:: ensure the algorithm name and version are compatible
if not [%_alg%]==[] (
  call :CHECKALGVER "%_alg%" "%_ver%" || exit /b
)
goto parseend

:get-dstdir
:: get output directory
call :GET_CHECK_DSTDIR "%arg%" || exit /b
set "_dstdir=%retval%\"
goto parseend

:get-ca-key-file
:: get CA private key file
call :NORMALIZEFILE "%arg%" || exit /b
set _ca_key="%retval%"
goto parseend

:get-ca-cert-file
:: get CA certificate file
call :NORMALIZEFILE "%arg%" || exit /b
set _ca_cert="%retval%"
goto parseend

:get-ca-alg
:: get CA algorithm name
call :CHECKALG "%arg%" || exit /b
set _ca_alg=%arg%
:: ensure the CA algorithm name and version are compatible
if not [%_ca_ver%]==[] (
  call :CHECKALGVER "%_ca_alg%" "%_ca_ver%" || exit /b
)
goto parseend

:get-ca-alg-ver
:: get CA algorithm version
set _ca_ver=%arg%
:: ensure the CA algorithm name and version are compatible
if not [%_ca_alg%]==[] (
  call :CHECKALGVER "%_ca_alg%" "%_ca_ver%" || exit /b
)
goto parseend

:get-server-key-file
:: get MySQL server private key file
call :NORMALIZEFILE "%arg%" || exit /b
set _server_key="%retval%"
goto parseend

:get-server-cert-file
:: get MySQL server certificate file
call :NORMALIZEFILE "%arg%" || exit /b
set _server_cert="%retval%"
goto parseend

:get-server-alg
:: get MySQL server algorithm name
call :CHECKALG "%arg%" || exit /b
set _server_alg=%arg%
:: ensure the MySQL server algorithm name and version are compatible
if not [%_server_ver%]==[] (
  call :CHECKALGVER "%_server_alg%" "%_server_ver%" || exit /b
)
goto parseend

:get-server-alg-ver
:: get MySQL server algorithm version
set _server_ver=%arg%
:: ensure the MySQL server algorithm name and version are compatible
if not [%_server_alg%]==[] (
  call :CHECKALGVER "%_server_alg%" "%_server_ver%" || exit /b
)
goto parseend

:get-client-key-file
:: get MySQL client private key file
call :NORMALIZEFILE "%arg%" || exit /b
set _client_key="%retval%"
goto parseend

:get-client-cert-file
:: get MySQL client certificate file
call :NORMALIZEFILE "%arg%" || exit /b
set _client_cert="%retval%"
goto parseend

:get-client-alg
:: get MySQL client algorithm name
call :CHECKALG "%arg%" || exit /b
set _client_alg=%arg%
:: ensure the MySQL client algorithm name and version are compatible
if not [%_client_ver%]==[] (
  call :CHECKALGVER "%_client_alg%" "%_client_ver%" || exit /b
)
goto parseend

:get-client-alg-ver
:: get MySQL client algorithm version
set _client_ver=%arg%
:: ensure the MySQL client algorithm name and version are compatible
if not [%_client_alg%]==[] (
  call :CHECKALGVER "%_client_alg%" "%_client_ver%" || exit /b
)
goto parseend

:get-web-key-file
:: get Tomcat web server private key file
call :NORMALIZEFILE "%arg%" || exit /b
set _web_key="%retval%"
goto parseend

:get-web-cert-file
:: get Tomcat web server certificate file
call :NORMALIZEFILE "%arg%" || exit /b
set _web_cert="%retval%"
goto parseend

:get-web-alg
:: get Tomcat web server algorithm name
call :CHECKALG "%arg%" || exit /b
set _web_alg=%arg%
:: ensure the Tomcat web server algorithm name and version are compatible
if not [%_web_ver%]==[] (
  call :CHECKALGVER "%_web_alg%" "%_web_ver%" || exit /b
)
goto parseend

:get-web-alg-ver
:: get Tomcat web server algorithm version
set _web_ver=%arg%
:: ensure the Tomcat web server algorithm name and version are compatible
if not [%_web_alg%]==[] (
  call :CHECKALGVER "%_web_alg%" "%_web_ver%" || exit /b
)
goto parseend

:get-priv-key-file
:: get MySQL password authentication private key file
call :NORMALIZEFILE "%arg%" || exit /b
set _priv_key="%retval%"
goto parseend

:get-pub-key-file
:: get MySQL password authentication public key file
call :NORMALIZEFILE "%arg%" || exit /b
set _pub_key="%retval%"
goto parseend

:get-priv-key-ver
:: get MySQL password authentication RSA version
set _priv_ver=%arg%
:: ensure MySQL password authentication version is compatible with RSA
call :CHECKALGVER "rsa" "%_priv_ver%" || exit /b

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
  call :usage 33
  exit /b
)


:: ========== set default values where necessary ==========

call :ALGVERDEFAULT rsa 2048 "%_alg%" "%_ver%" || exit /b
set "_alg=%retalg%"
set "_ver=%retver%"

if [!_dstdir!]==[] (
  call :NORMALIZEPATH "." || exit /b
  set "_dstdir="
)

if [!_ca_key!]==[] (
  call :NORMALIZEFILE "%_dstdir%ca-key.pem" || exit /b
  set _ca_key="!retval!"
)
if [!_ca_cert!]==[] (
  call :NORMALIZEFILE "%_dstdir%ca.pem" || exit /b
  set _ca_cert="!retval!"
)
call :ALGVERDEFAULT "%_alg%" "%_ver%" "%_ca_alg%" "%_ca_ver%" || exit /b
set "_ca_alg=%retalg%"
set "_ca_ver=%retver%"


if [!_server_key!]==[] (
  call :NORMALIZEFILE "%_dstdir%server-key.pem" || exit /b
  set _server_key="!retval!"
)
if [!_server_cert!]==[] (
  call :NORMALIZEFILE "%_dstdir%server-cert.pem" || exit /b
  set _server_cert="!retval!"
)
call :ALGVERDEFAULT "%_alg%" "%_ver%" "%_server_alg%" "%_server_ver%" || exit /b
set "_server_alg=%retalg%"
set "_server_ver=%retver%"


if [!_client_key!]==[] (
  call :NORMALIZEFILE "%_dstdir%client-key.pem" || exit /b
  set _client_key="!retval!"
)
if [!_client_cert!]==[] (
  call :NORMALIZEFILE "%_dstdir%client-cert.pem" || exit /b
  set _client_cert="!retval!"
)
call :ALGVERDEFAULT "%_alg%" "%_ver%" "%_client_alg%" "%_client_ver%" || exit /b
set "_client_alg=%retalg%"
set "_client_ver=%retver%"


if [!_web_key!]==[] (
  call :NORMALIZEFILE "%_dstdir%web-key.pem" || exit /b
  set _web_key="!retval!"
)
if [!_web_cert!]==[] (
  call :NORMALIZEFILE "%_dstdir%web-cert.pem" || exit /b
  set _web_cert="!retval!"
)
call :ALGVERDEFAULT "%_alg%" "%_ver%" "%_web_alg%" "%_web_ver%" || exit /b
set "_web_alg=%retalg%"
set "_web_ver=%retver%"


if [!_priv_key!]==[] (
  call :NORMALIZEFILE "%_dstdir%private_key.pem" || exit /b
  set _priv_key="!retval!"
)
if [!_pub_key!]==[] (
  call :NORMALIZEFILE "%_dstdir%public_key.pem" || exit /b
  set _pub_key="!retval!"
)
if [!_priv_ver!]==[] (
  set _priv_ver=2048
)

:: ========== generate keys and certificates ==========

set "_rootpath=%~dp0"
set _ca_conf="%_rootpath%\ca-cert.cnf"
set _server_conf="%_rootpath%\server-cert.cnf"
set _client_conf="%_rootpath%\client-cert.cnf"
set _web_conf="%_rootpath%\web-cert.cnf"

set _server_req="%TEMP%\smalltownships-server-req%RANDOM%.pem"
set _client_req="%TEMP%\smalltownships-client-req%RANDOM%.pem"
set _web_req="%TEMP%\smalltownships-web-req%RANDOM%.pem"

if /I %_ca_alg%==rsa (
  echo Generating RSA %_ca_ver% CA Private Key %_ca_key%
  openssl genrsa -out %_ca_key% %_ca_ver% 2>nul
) else if /I %_ca_alg%==dsa (
  echo Generating DSA %_ca_ver% CA Private Key %_ca_key%
  echo This may take some time
  openssl dsaparam -genkey -out %_ca_key% %_ca_ver% 2>nul
) else (
  echo Generating ECDSA CA Private Key %_ca_key% using Named Curve %_ca_ver%
  openssl ecparam -genkey -name %_ca_ver% 2>nul | openssl ec -out %_ca_key% 2>nul
)
if %ERRORLEVEL% NEQ 0 (
  echo Could not generate CA Private Key >&2
  exit /b 34
)

if /I %_server_alg%==rsa (
  echo Generate RSA %_server_ver% MySQL Server Private Key %_server_key%
  openssl genrsa -out %_server_key% %_server_ver% 2>nul
) else if /I %_server_alg%==dsa (
  echo Generating DSA %_server_ver% MySQL Server Private Key %_server_key%
  echo This may take some time
  openssl dsaparam -genkey -out %_server_key% %_server_ver% 2>nul
) else (
  echo Generating ECDSA MySQL Server Private Key %_server_key% using Named Curve %_server_ver%
  openssl ecparam -genkey -name %_server_ver% 2>nul | openssl ec -out %_server_key% 2>nul
)
if %ERRORLEVEL% NEQ 0 (
  echo Could not generate MySQL Server Private Key >&2
  exit /b 35
)

if /I %_client_alg%==rsa (
  echo Generate RSA %_client_ver% MySQL Client Private Key %_client_key%
  openssl genrsa -out %_client_key% %_client_ver% 2>nul
) else if /I %_client_alg%==dsa (
  echo Generating DSA %_client_ver% MySQL Client Private Key %_client_key%
  echo This may take some time
  openssl dsaparam -genkey -out %_client_key% %_client_ver% 2>nul
) else (
  echo Generating ECDSA MySQL Client Private Key %_client_key% using Named Curve %_client_ver%
  openssl ecparam -genkey -name %_client_ver% 2>nul | openssl ec -out %_client_key% 2>nul
)
if %ERRORLEVEL% NEQ 0 (
  echo Could not generate MySQL Client Private Key >&2
  exit /b 36
)

if /I %_web_alg%==rsa (
  echo Generate RSA %_web_ver% Tomcat Web Server Private Key %_web_key%
  openssl genrsa -out %_web_key% %_web_ver% 2>nul
) else if %_web_alg% == dsa (
  echo Generating DSA %_web_ver% Tomcat Web Server Private Key %_web_key%
  echo This may take some time
  openssl dsaparam -genkey -out %_web_key% %_web_ver% 2>nul
) else (
  echo Generating ECDSA Tomcat Web Server Private Key %_web_key% using Named Curve %_web_ver%
  openssl ecparam -genkey -name %_web_ver% 2>nul | openssl ec -out %_web_key% 2>nul
)
if %ERRORLEVEL% NEQ 0 (
  echo Could not generate Tomcat Web Server Private Key >&2
  exit /b 37
)

set "_cleanup="

echo:
echo Generating CA Self-Signed Root Certificate %_ca_cert%
openssl req -new -x509 -days 3650 -key %_ca_key% -out %_ca_cert% -set_serial 00 -batch -config %_ca_conf% 2>nul || (
  echo Could not generate CA Root Certificate >&2
  exit /b 38
)

echo Generating MySQL Server Certificate %_server_cert%
openssl req -new -key %_server_key% -out %_server_req% -batch -config %_server_conf% 2>nul || (
  echo Could not generate MySQL Server Certificate Request >&2
  exit /b 39
)
set "_cleanup=%_server_req%"
openssl x509 -req -days 3650 -in %_server_req% -CA %_ca_cert% -CAkey %_ca_key% -set_serial 01 -out %_server_cert% 2>nul || (
  echo Could not generate MySQL Server Certificate >&2
  del %_cleanup%
  exit /b 40
)

echo Generating MySQL Client Certificate %_client_cert%
openssl req -new -key %_client_key% -out %_client_req% -batch -config %_client_conf% 2>nul || (
  echo Could not generate MySQL Client Certificate Request >&2
  del %_cleanup%
  exit /b 41
)
set "_cleanup=%_cleanup% %_client_req%"
openssl x509 -req -days 3650 -in %_client_req% -CA %_ca_cert% -CAkey %_ca_key% -set_serial 02 -out %_client_cert% 2>nul || (
  echo Could not generate MySQL Client Certificate >&2
  del %_cleanup%
  exit /b 42
)

echo Generating Tomcat Web Server Certificate %_web_cert%
openssl req -new -key %_web_key% -out %_web_req% -batch -config %_web_conf% 2>nul || (
  echo Could not generate Tomcat Web Server Certificate Request >&2
  exit /b 43
)
set "_cleanup=%_cleanup% %_web_req%"
openssl x509 -req -days 3650 -in %_web_req% -CA %_ca_cert% -CAkey %_ca_key% -set_serial 03 -out %_web_cert% 2>nul || (
  echo Could not generate Tomcat Web Server Certificate >&2
  del %_cleanup%
  exit /b 44
)

echo:
echo Generating MySQL Password Authentication RSA %_priv_ver% Private Key %_priv_key%
openssl genrsa -out %_priv_key% %_priv_ver% 2>nul || (
  echo Could not generate MySQL Password Authentication Private Key >&2
  del %_cleanup%
  exit /b 45
)
echo Extracting MySQL Password Authentication RSA %_priv_ver% Public Key %_pub_key%
openssl rsa -in %_priv_key% -pubout -out %_pub_key% 2>nul || (
  echo Could not extract MySQL Password Authentication Public Key >&2
  del %_cleanup%
  exit /b 46
)
echo:
echo Generation Complete

del %_cleanup%

endlocal

@echo on
