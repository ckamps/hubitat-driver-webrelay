# Hubitat WebRelay Driver

Hubitat driver for the [WebRelay](https://www.controlbyweb.com/webrelay/) device sold by Control by Web.

## Example commands

### Get current state

```
$ curl --http0.9 http://192.168.2.23/state.xml

<?xml version="1.0" encoding="utf-8"?>
<datavalues>
<relaystate>0</relaystate>
<inputstate>0</inputstate>
<rebootstate>0</rebootstate>
<totalreboots>0</totalreboots>
</datavalues>
```

Using `stateFull.xml` to receive HTTP headers on the response:
```
$ curl --http0.9 "http://192.168.2.23/stateFull.xml"

<?xml version="1.0" encoding="utf-8" ?> <datavalues><relaystate>0</relaystate><inputstate>0</inputstate><rebootstate>0</rebootstate><failures>0</failures><rbtAttempts>0</rbtAttempts><totalreboots>0</totalreboots><devmode>0</devmode><maxrbts>10</maxrbts></datavalues>
```

### Turn on relay

```
$ curl --http0.9 "http://192.168.2.23/stateFull.xml?relayState=1"

<?xml version="1.0" encoding="utf-8" ?> <datavalues><relaystate>1</relaystate><inputstate>0</inputstate><rebootstate>0</rebootstate><failures>0</failures><rbtAttempts>0</rbtAttempts><totalreboots>0</totalreboots><devmode>0</devmode><maxrbts>10</maxrbts></datavalues>
```

### Turn off relay

```
$ curl --http0.9 "http://192.168.2.23/stateFull.xml?relayState=0"

<?xml version="1.0" encoding="utf-8" ?> <datavalues><relaystate>0</relaystate><inputstate>0</inputstate><rebootstate>0</rebootstate><failures>0</failures><rbtAttempts>0</rbtAttempts><totalreboots>0</totalreboots><devmode>0</devmode><maxrbts>10</maxrbts></datavalues>
```