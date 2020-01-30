#!/bin/sh

pushd ~/.xnatgateway &> /dev/null; gd=`pwd`; popd &> /dev/null

if [ ! -f "$gd/gateway.properties.console" ]; then
	echo "XNAT gateway error: cannot find configuration file $gd/gateway.properties.console"
	echo "To set up console server, copy ./config/gateway.properties.default to $gd/gateway.properties.console and manually edit it"
	exit -1
fi

java -Xms128m -Xmx256m -cp "./dist/gatewaye.jar:./dist/lib/commons-codec-1.3.jar:./dist/lib/commons-httpclient-3.1.jar:./dist/lib/commons-io-1.4.jar:./dist/lib/commons-lang-2.4.jar:./dist/lib/commons-logging-1.1.1.jar:./dist/lib/dcm4che.jar:/lib/dcm4che-audit-2.0.21.jar:./dist/lib/dom4j-1.6.1.jar:./dist/lib/jai_imageio_api.jar:./dist/lib/log4j-1.2.15.jar:./dist/lib/xdat-beans-1.0.jar:./dist/lib/xnd-beans.jar" org.nrg.xnat.gateway.XNATGatewayServer c
