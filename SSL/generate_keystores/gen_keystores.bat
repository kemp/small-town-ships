@echo off

setlocal

keytool 2>nul || (
	echo Could not find Java's keytool utility
	echo Please ensure Java is configured in the PATH environment variable
	exit /b 1
)

if not [%1] == [] (
	set _trustpass=%~1
	if not [%2] == [] (
		set _keypass=%~2
		if not [%3] == [] (
			set _srcdir=%~f3
			if not [%4] == [] (
				set _dstdir=%~f4
			) else (
				set _dstdir=.
			)
		) else (
			set _srcdir=.
			set _dstdir=.
		)
	) else (
		echo No keystore password provided
		echo Assuming the truststore and keystore use the same password
		echo:
		set _keypass=%~1
		set _srcdir=.
		set _dstdir=.
	)
) else (
	echo No truststore or keystore password provided
	exit /b 1
)

echo Generating truststore and keystore files with settings-
echo truststore password:%_trustpass%
echo keystore password:%_keypass%
echo input certificate directory:%_srcdir%
echo output truststore and keystore directory:%_dstdir%
echo:

set _issuer=SmallTownShipsInc_MySQL_issuer
set _server=SmallTownShipsInc_MySQL_server
set _client=SmallTownShipsInc_MySQL_client

set _ca_cert="%_srcdir%\ca.pem"
set _server_cert="%_srcdir%\server-cert.pem"
set _client_cert="%_srcdir%\client-cert.pem"
set _client_key="%_srcdir%\client-key.pem"

set _truststore=%_dstdir%\truststore
set _keystore=%_dstdir%\keystore

echo Generating truststore
if exist %_truststore% (
	keytool -delete -alias %_issuer% -keystore %_truststore% -storepass %_trustpass% >nul 2>&1 || (
		echo Could not ensure the CA certificate does not already exist in the truststore
		echo Perhaps the truststore password is incorrect?
		exit /b 1
	)
	keytool -delete -alias %_server% -keystore %_truststore% -storepass %_trustpass% >nul 2>&1 || (
		echo Could not ensure the server certificate does not already exist in the truststore
		exit /b 1
	)
)
keytool -importcert -noprompt -alias %_issuer% -file %_ca_cert% -keystore "%_truststore%" -storepass %_trustpass% >nul 2>&1 || (
	echo Could not generate truststore
	exit /b 1
)
keytool -importcert -noprompt -alias %_server% -file %_server_cert% -keystore "%_truststore%" -storepass %_trustpass% >nul 2>&1 || (
	echo Could not generate truststore
	exit /b 1
)
echo truststore created at %_truststore%

echo:
echo Generating keystore
if exist %_keystore% (
	keytool -delete -alias %_client% -keystore "%_keystore%" -storepass %_keypass% >nul 2>&1 || (
		echo Could not ensure the client certificate and private key do not already exist in the keystore
		echo Perhaps the keystore password is incorrect?
		exit /b 1
	)
)
openssl pkcs12 -export -in %_client_cert% -inkey %_client_key% -name %_client% -passout pass:%_keypass% -out client-keystore.p12 >nul || (
	echo Could not generate PKCS #12 archive for the client certificate and private key
	exit /b 1
)
keytool -importkeystore -noprompt -srckeystore client-keystore.p12 -srcstoretype pkcs12 -srcstorepass %_keypass% -destkeystore "%_keystore%" -deststoretype jks -deststorepass %_keypass% >nul 2>&1 || (
	echo Could not generate keystore
	exit /b 1
)
del client-keystore.p12
echo keystore created at %_keystore%

echo:
echo Generation complete

endlocal

@echo on