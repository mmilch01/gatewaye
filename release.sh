#!/bin/bash

WIN=/cygdrive/c/Workspace/GatewaySetup/win
LIN=/cygdrive/c/Workspace/GatewaySetup/linux
MACOS=/cygdrive/c/Workspace/GatewaySetup/MacOS/Gateway-GUI.app/Contents/MacOS/Gateway
SRC=/cygdrive/c/Workspace/xnd_stable/gatewaye

cp -rf gatewaye.jar README.TXT $WIN/dist
cp -rf config $WIN
cp -rf lib $WIN/dist

cp -rf gatewaye.jar README.TXT $LIN/dist
cp -rf config $LIN
cp -rf lib $LIN/dist

cp -rf gatewaye.jar README.TXT $MACOS/dist
cp -rf config $MACOS
cp -rf lib $MACOS/dist

ver=`date +"%b%Y"`
ISETUP="/cygdrive/c/Program Files (x86)/Inno Setup 5/ISCC.exe"
pushd /cygdrive/c/Workspace/GatewaySetup/
"$ISETUP" gateway.iss
"$ISETUP" gateway-bundled.iss

T=/cygdrive/y/ftp/pub/xnd/download/gw
mv -f $T/XNAT_Gateway_* $T/old

pushd linux
rm *.zip
chmod -R o+rwx *
zip -r XNAT_Gateway_Linux_${ver}.zip *
chmod o+rwx XNAT_Gateway_Linux_${ver}.zip
cp -f XNAT_Gateway_Linux_${ver}.zip $T
popd 

pushd MacOS
rm *.zip
chmod -R o+rwx *
zip -r XNAT_Gateway_MacOS_${ver}.zip *
chmod o+rwx XNAT_Gateway_MacOS_${ver}.zip
cp -f XNAT_Gateway_MacOS_${ver}.zip $T
popd

pushd win/Output
chmod -R o+rwx *
cp gatewaysetup-jre.exe $T/XNAT_Gateway_Win_${ver}_JRE_Bundled.exe
cp gatewaysetup.exe $T/XNAT_Gateway_Win_${ver}.exe
popd

popd


