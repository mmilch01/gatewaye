#!/bin/bash
set -x
umask 0007
WIN=/cygdrive/c/src/eclipse/GatewaySetup/win; mkdir -p $WIN
LIN=/cygdrive/c/src/eclipse/GatewaySetup/linux; mkdir -p $LIN
MACOS=/cygdrive/c/src/eclipse/GatewaySetup/MacOS/Gateway-GUI.app/Contents/MacOS/Gateway; mkdir -p $MACOS
SRC=/cygdrive/c/src/eclipse/gatewaye
SRC_SETUP=/cygdrive/c/src/eclipse/GatewaySetup


cp -rf gatewaye.jar README.TXT $WIN/dist
cp -rf config $WIN
cp -rf lib $WIN/dist

cp -rf gatewaye.jar README.TXT $LIN/dist
cp -rf config $LIN
cp -rf lib $LIN/dist

cp -rf gatewaye.jar README.TXT $MACOS/dist
cp -rf config $MACOS
cp -rf lib $MACOS/dist


read -p "continue (y/n)?" status
if [ "$status" == "n" ]; then exit 1; fi

ver=`date +"%b%Y"`
ISETUP="/cygdrive/c/Program Files (x86)/Inno Setup 5/ISCC.exe"
pushd $SRC_SETUP
chmod -R 777 *
"$ISETUP" gateway.iss
chmod -R 777 *
"$ISETUP" gateway-bundled.iss
chmod -R 777 *

read -p "continue (y/n)?" status
if [ "$status" == "n" ]; then exit 1; fi

T=/cygdrive/y/ftp/pub/xnd/download/gw
mv -f $T/XNAT_Gateway_* $T/old

read -p "continue (y/n)?" status
if [ "$status" == "n" ]; then exit 1; fi


pushd linux
rm *.zip
chmod -R o+rwx *
zip -r XNAT_Gateway_Linux_${ver}.zip *
chmod o+rwx XNAT_Gateway_Linux_${ver}.zip
cp -f XNAT_Gateway_Linux_${ver}.zip $T
popd 

read -p "continue (y/n)?" status
if [ "$status" == "n" ]; then exit 1; fi


pushd MacOS
rm *.zip
chmod -R o+rwx *
zip -r XNAT_Gateway_MacOS_${ver}.zip *
chmod o+rwx XNAT_Gateway_MacOS_${ver}.zip
cp -f XNAT_Gateway_MacOS_${ver}.zip $T
popd

read -p "continue (y/n)?" status
if [ "$status" == "n" ]; then exit 1; fi

pushd win/Output
chmod -R o+rwx *
cp gatewaysetup-jre.exe $T/XNAT_Gateway_Win_${ver}_JRE_Bundled.exe
cp gatewaysetup.exe $T/XNAT_Gateway_Win_${ver}.exe
popd

popd


