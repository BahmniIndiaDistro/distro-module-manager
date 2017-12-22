#!/bin/sh

rm -f /etc/init.d/distro-module-manager
rm -f /etc/default/distro-module-manager
rm -f /var/run/distro-module-manager

#Remove distro-module-manager from chkconfig
chkconfig --del distro-module-manager || true
