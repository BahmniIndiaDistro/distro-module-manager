#!/bin/sh

ln -s /opt/distro-module-manager/bin/distro-module-manager /etc/init.d/distro-module-manager
ln -s /opt/distro-module-manager/etc/distro-module-manager /etc/default/distro-module-manager
ln -s /opt/distro-module-manager/var /var/run/distro-module-manager

if [ ! -e /var/log/distro-module-manager ]; then
    mkdir /var/log/distro-module-manager
fi

# Add distro-module-manager service to chkconfig
chkconfig --add distro-module-manager
