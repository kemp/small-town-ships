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
	set _dstdir=./
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

if /I %_alg% == rsa (
	echo Generating RSA %_ver% Keys and Certificates
	echo:
	echo Generate CA Private Key
	openssl genrsa -out %_dstdir%\ca-key.pem %_ver% 2>nul || (
		echo Could not generate CA Private Key
		exit /b 1
	)
	echo Generate Server Private Key
	openssl genrsa -out %_dstdir%\server-key.pem %_ver% 2>nul || (
		echo Could not generate Server Private Key
		exit /b 1
	)
	echo Generate Client Private Key
	openssl genrsa -out %_dstdir%\client-key.pem %_ver% 2>nul || (
		echo Could not generate Client Private Key
		exit /b 1
	)
) else if /I %_alg% == dsa (
	echo Generating DSA %_ver% Keys and Certificates
	echo This may take a while
	echo:
	echo Generate CA Private Key
	openssl dsaparam -genkey -out %_dstdir%\ca-key.pem %_ver% 2>nul || (
		echo Could not generate CA Private Key
		exit /b 1
	)
	echo Generate Server Private Key
	openssl dsaparam -genkey -out %_dstdir%\server-key.pem %_ver% 2>nul || (
		echo Could not generate Server Private Key
		exit /b 1
	)
	echo Generate Client Private Key
	openssl dsaparam -genkey -out %_dstdir%\client-key.pem %_ver% 2>nul || (
		echo Could not generate Client Private Key
		exit /b 1
	)
) else (
	echo Generating ECDSA Keys and Certificates using Named Curve %_ver%
	echo:
	echo Generate CA Private Key
	openssl ecparam -genkey -name %_ver% 2>nul | openssl ec -out %_dstdir%\ca-key.pem 2>nul || (
		echo Could not generate CA Private Key
		echo Perhaps %_ver% is not a valid curve?
		exit /b 1
	)
	echo Generate Server Private Key
	openssl ecparam -genkey -name %_ver% 2>nul | openssl ec -out %_dstdir%\server-key.pem 2>nul || (
		echo Could not generate Server Private Key
		exit /b 1
	)
	echo Generate Client Private Key
	openssl ecparam -genkey -name %_ver% 2>nul | openssl ec -out %_dstdir%\client-key.pem 2>nul || (
		echo Could not generate Client Private Key
		exit /b 1
	)
)

echo:
echo Generate CA Root Certificate
openssl req -new -x509 -days 3650 -key %_dstdir%\ca-key.pem -out %_dstdir%\ca.pem -batch -config ca-cert.cnf 2>nul || (
	echo Could not generate CA Root Certificate
	exit /b 1
)
echo Generate Server Certificate Request
openssl req -new -key %_dstdir%\server-key.pem -out %_dstdir%\server-req.pem -batch -config server-cert.cnf 2>nul || (
	echo Could not generate Server Certificate Request
	exit /b 1
)
echo Generate Client Certificate Request
openssl req -new -key %_dstdir%\client-key.pem -out %_dstdir%\client-req.pem -batch -config client-cert.cnf 2>nul || (
	echo Could not generate Client Certificate Request
	exit /b 1
)
echo Generate Server Certificate
openssl x509 -req -days 3650 -in %_dstdir%\server-req.pem -CA %_dstdir%\ca.pem -CAkey %_dstdir%\ca-key.pem -set_serial 02 -out %_dstdir%\server-cert.pem 2>nul || (
	echo Could not generate Server Certificate Request
	exit /b 1
)
echo Generate Client Certificate
openssl x509 -req -days 3650 -in %_dstdir%\client-req.pem -CA %_dstdir%\ca.pem -CAkey %_dstdir%\ca-key.pem -set_serial 03 -out %_dstdir%\client-cert.pem 2>nul || (
	echo Could not generate Client Certificate Request
	exit /b 1
)
echo:
echo Generating RSA Authentication Key Pair
echo Generate RSA 2048 Private Key
openssl genrsa -out %_dstdir%\private_key.pem 2048 2>nul || (
	echo Could not generate RSA Authentication Private Key
	exit /b 1
)
echo Extract RSA 2048 Public Key
openssl rsa -in %_dstdir%\private_key.pem -pubout -out %_dstdir%\public_key.pem 2>nul || (
	echo Could not extract RSA Authentication Public Key
)
echo:
echo Generation Complete

endlocal

@echo on