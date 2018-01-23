# distro-module-manager
This is a utility service which is used to install specific modules for india-distro. Before going ahead please make sure you have installed the base india-distro and the analytics app. Checkout the [README](https://github.com/BahmniIndiaDistro/distro/blob/master/README.md) here.

#### How to install:-
```
#Checkout the latest version of the repository.
git clone git@github.com:BahmniIndiaDistro/distro-module-manager.git

#Go to the repository folder.
cd distro-module-manager

#Build the RPM using gradle
./gradlew clean dist

#Copy the RPM to the bahmni box, Assuming you have a vagrant box running on 192.168.33.10
scp build/distributions/distro-module-manager-0.1.noarch.rpm vagrant@192.168.33.10:/tmp

#SSH into vagrant machine. Go to the temp folder and install the RPM
ssh vagrant@192.169.33.10
cd /tmp
rpm -i distro-module-manager-0.1.noarch.rpm
``` 

> The distro is bundled into RPM and will be exploded into `/opt/distro-module-manager/distro/`. The service will use this distro folder while installing modules.
 

#### How to use
Before using the service you need to make changes to `/etc/default/distro-module-manager` file. This will be used as configuration file.
Below are the config options in the file. They have been given values based on conventions. The values written with `<>` need to be replaced.
```
INDIA_DISTRO_MODULES_DIR=/opt/distro-module-manager/distro/modules
BAHMNI_CONFIG_DIR=/var/www/bahmni_config/
OPENMRS_BASE_URL=<https://192.168.33.10/>
OPENMRS_API_USERNAME=<username>
OPENMRS_API_PASSWORD=<12345>
WAIT_INTERVAL_FOR_CSV_UPLOAD=3000
LOG_LEVEL=DEBUG
```
Since installing a module requires copying few files to different locations, you need to switch to root user while running the service.
Before using the service, you will need to source the configuration file. As of now the service doesn't run continuously and need to be invoked manually.
```
source /etc/default/distro-module-manager
java -jar /opt/distro-module-manager/lib/distro-module-manager-0.1-SNAPSHOT.jar install <Module_Name> 
```




