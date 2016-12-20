Azure Event Hub with Zombies
========================================================

##We are [CAPSiDE](http://twitter.com/capside)

##Introduction

* Here are the main [slides](http://slides.com/capside/zombies#/)

##Setup

In order to use this code you'll need an active Microsoft Azure Subscription - if you don't have one you can obtain some free credit here:

* https://www.visualstudio.com/dev-essentials/

Roughly the steps are as follows:

1. Login to the Azure portal
2. Click the green + top left to add a new service
3. Search for "Event Hubs", select it and click 'CREATE'
4. First it will prompt you to create a new Service Bus namespace and create a new resource group, make a note of the namespace name, we'll refer to this as $NAMESPACE later.
5. Once the namespace is created, find the new Event Hub namespace in your resource group, on the configuration blade you will see an 'Entities' section with 'Event Hubs, select this and click the + Event Hub, give it a name - we'll refer to this as $HUB later, but leave all else as default.
6. Once hub is created, find your new hub under the namespace, click to select it and click 'Shared access policies'
7. Click + Add Key, we'll need two, create one with only the 'Send' claim and one with only the 'Listen' claim, make a note of the names, we'll refer to these as $SENDKEY and $LISTENKEY
8. Go back to your resource group containing the Event Hub namespace and click + Add to add another Azure resource, this time search for Storage and create a new 'Storage account', remember the name, we'll refer to this as $STORAGEACCOUNT and also be sure to select "Locally-redundant storage (LRS)" as this will save some cost.
9. Find the new storage account blade for the newly created storage, under 'Blob Service' click + Container and make a note of the name, we'll refer to that as $CONTAINER 
10. Finally, go back to the storage blade and under 'Settings' click 'Access keys' and make a note of key1, we'll refer to this as $STORAGEKEY

You should now have all the ingredients needed to run the following.

VERY IMPORTANT - don't forget to delete the resource group once you have finished, this will automatically delete all the resources underneath - to prevent costs mounting up!

##Running


java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044 -jar target/ZombieProducer-0.0.2-SNAPSHOT.jar --drone=5555 --latitude=40.415363 --longitude=-3.707398 --ns=$NAMESPACE --hub=$HUB --keyname=$SENDKEY --key="$HUBKEY"

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1045 -jar target/ZombieConsumer-0.0.2-SNAPSHOT.jar --ns=$NAMESPACE --hub=$HUB --keyname=$KEY --key="$LISTENKEY" --storageaccount=$STORAGEACCOUNT --storagekey="$STORAGEKEY" --storagecontainer=$CONTAINER

