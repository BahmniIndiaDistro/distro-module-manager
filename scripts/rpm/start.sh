#!/bin/sh
nohup java -jar /opt/distro-module-manager/lib/distro-module-manager.jar >  /dev/null 2>&1 &
echo $! > /var/run/distro-module-manager/distro-module-manager.pid
