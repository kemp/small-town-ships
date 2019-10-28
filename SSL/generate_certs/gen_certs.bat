@echo off

setlocal

openssl version >nul || (
	echo Please ensure that OpenSSL is installed and configured
	echo in the PATH environment variable
	exit /b 1
)

if not [%1] == [] (
	set _dstdir=%~f1
	if not [%2] == [] (
		set _alg=%2
		if not [%3] == [] (
			set _ver=%3
		)
	)
)

if not defined _dstdir (
	set _dstdir=.
)
if not defined _alg (
	set _alg=rsa
)
if not defined _ver (
	if /I %_alg% == dsa (
		set _ver=2048
	) else if /I %_alg% == ecdsa (
		set _ver=secp256r1
	) else if /I %_alg% == rsa (
		set _ver=2048
	) else (
		echo Unrecognized Algorithm: %_alg%
		echo Must be one of: rsa, dsa, or ecdsa
		exit /b 1
	)
)

set _rootpath=%~dp0
set _ca_conf="%_rootpath%\ca-cert.cnf"
set _server_conf="%_rootpath%\server-cert.cnf"
set _client_conf="%_rootpath%\client-cert.cnf"

set _ca="%_dstdir%\ca.pem"
set _ca_key="%_dstdir%\ca-key.pem"
set _server_cert="%_dstdir%\server-cert.pem"
set _server_req="%_dstdir%\server-req.pem"
set _server_key="%_dstdir%\server-key.pem"
set _client_cert="%_dstdir%\client-cert.pem"
set _client_req="%_dstdir%\client-req.pem"
set _client_key="%_dstdir%\client-key.pem"

set _priv="%_dstdir%\private_key.pem"
set _pub="%_dstdir%\public_key.pem"

if /I %_alg% == rsa (
	echo Generating RSA %_ver% Keys and Certificates
	echo:
	echo Generate CA Private Key
	openssl genrsa -out %_ca_key% %_ver% 2>nul || (
		echo Could not generate CA Private Key
		exit /b 1
	)
	echo Generate Server Private Key
	openssl genrsa -out %_server_key% %_ver% 2>nul || (
		echo Could not generate Server Private Key
		exit /b 1
	)
	echo Generate Client Private Key
	openssl genrsa -out %_client_key% %_ver% 2>nul || (
		echo Could not generate Client Private Key
		exit /b 1
	)
) else if /I %_alg% == dsa (
	echo Generating DSA %_ver% Keys and Certificates
	echo This may take a while
	echo:
	echo Generate CA Private Key
	openssl dsaparam -genkey -out %_ca_key% %_ver% 2>nul || (
		echo Could not generate CA Private Key
		exit /b 1
	)
	echo Generate Server Private Key
	openssl dsaparam -genkey -out %_server_key% %_ver% 2>nul || (
		echo Could not generate Server Private Key
		exit /b 1
	)
	echo Generate Client Private Key
	openssl dsaparam -genkey -out %_client_key% %_ver% 2>nul || (
		echo Could not generate Client Private Key
		exit /b 1
	)
) else (
	echo Generating ECDSA Keys and Certificates using Named Curve %_ver%
	echo:
	echo Generate CA Private Key
	openssl ecparam -genkey -name %_ver% 2>nul | openssl ec -out %_ca_key% 2>nul || (
		echo Could not generate CA Private Key
		echo Perhaps %_ver% is not a valid curve?
		exit /b 1
	)
	echo Generate Server Private Key
	openssl ecparam -genkey -name %_ver% 2>nul | openssl ec -out %_server_key% 2>nul || (
		echo Could not generate Server Private Key
		exit /b 1
	)
	echo Generate Client Private Key
	openssl ecparam -genkey -name %_ver% 2>nul | openssl ec -out %_client_key% 2>nul || (
		echo Could not generate Client Private Key
		exit /b 1
	)
)

echo:
echo Generate CA Root Certificate
openssl req -new -x509 -days 3650 -key %_ca_key% -out %_ca% -batch -config %_ca_conf% 2>nul || (
	echo Could not generate CA Root Certificate
	exit /b 1
)
echo Generate Server Certificate Request
openssl req -new -key %_server_key% -out %_server_req% -batch -config %_server_conf% 2>nul || (
	echo Could not generate Server Certificate Request
	exit /b 1
)
echo Generate Client Certificate Request
openssl req -new -key %_client_key% -out %_client_req% -batch -config %_client_conf% 2>nul || (
	echo Could not generate Client Certificate Request
	exit /b 1
)
echo Generate Server Certificate
openssl x509 -req -days 3650 -in %_server_req% -CA %_ca% -CAkey %_ca_key% -set_serial 02 -out %_server_cert% 2>nul || (
	echo Could not generate Server Certificate Request
	exit /b 1
)
echo Generate Client Certificate
openssl x509 -req -days 3650 -in %_client_req% -CA %_ca% -CAkey %_ca_key% -set_serial 03 -out %_client_cert% 2>nul || (
	echo Could not generate Client Certificate Request
	exit /b 1
)
echo:
echo Generating RSA Authentication Key Pair
echo Generate RSA 2048 Private Key
openssl genrsa -out %_priv% 2048 2>nul || (
	echo Could not generate RSA Authentication Private Key
	exit /b 1
)
echo Extract RSA 2048 Public Key
openssl rsa -in %_priv% -pubout -out %_pub% 2>nul || (
	echo Could not extract RSA Authentication Public Key
)
echo:
echo Generation Complete

endlocal

@echo on