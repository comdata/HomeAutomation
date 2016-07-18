# Scheduler

The scheduler can be used to control the following aspects of the system:

- update Weather information periodically
- perform actor state changes (e.g. switch on/off)
- move window blinds (shading in percent)

## general configuration file structure

The format follows the general cron format (see [Wikipedia Cron](https://en.wikipedia.org/wiki/Cron) for details)

save a schedule.cron file in /var/lib/tomcat8/webapps/HomeAutomation

```
┌───────────── min (0 - 59)
│ ┌────────────── hour (0 - 23)
│ │ ┌─────────────── day of month (1 - 31)
│ │ │ ┌──────────────── month (1 - 12)
│ │ │ │ ┌───────────────── day of week (0 - 6) (0 to 6 are Sunday to
│ │ │ │ │                  Saturday, or use names; 7 is also Sunday)
│ │ │ │ │
│ │ │ │ │
* * * * *  command to execute
```

Command to execute can be any static Java method that accepts only a String[] as input.

The format is:

```
java:<package name>.<class name>#<method name>
```

## periodic weather update
```
*/5 * * * * java:cm.homeautomation.services.base.WeatherDataThread#loadWeather
```

## window blind movement

## actor changes