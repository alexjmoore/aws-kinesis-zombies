EventHub with Zombies
========================================================

##We are [Capside](http://twitter.com/capside)

* And we are kinda nice

![Drink and learn](https://pbs.twimg.com/media/ClfvYdOXIAAj1jK.jpg:large)

##Introduction

* Here are the main [slides](http://slides.com/capside/zombies#/)

##Running


java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1044 -jar target/ZombieProducer-0.0.2-SNAPSHOT.jar --drone=5555 --latitude=40.415363 --longitude=-3.707398 --ns=<NAMESPACE> --ns --hub=<HUB> --keyname=<KEY> --key="<HUBKEY>"

java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=1045 -jar target/ZombieConsumer-0.0.2-SNAPSHOT.jar --ns=<NAMESPACE> --hub=<HUB> --keyname=<KEY> --key="<HUBKEY>" --storageaccount=<STORAGEACCOUNT> --storagekey="<STORAGEKEY>" --storagecontainer=<CONTAINER>

