#!/bin/sh

java -Xdebug -Xrunjdwp:transport=dt_socket,address=8000,suspend=n,server=y -cp "./bin:./lib/commons-codec-1.3.jar:./lib/commons-httpclient-3.1.jar:./lib/commons-io-1.4.jar:./lib/commons-lang-2.4.jar:./lib/commons-logging-1.1.1.jar:./lib/dcm4che.jar:/lib/dcm4che-audit-2.0.21.jar:./lib/dom4j-1.6.1.jar:./lib/jai_imageio_api.jar:./lib/log4j-1.2.15.jar:./lib/xdat-beans-1.0.jar:./lib/xnd-beans.jar" org.nrg.xnat.gateway.XNATGatewayServer $*
