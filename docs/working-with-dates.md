Gorm-tools provides a set of static utils which allow us to manipulate with dates much easier.

## DateUtil

See [DateUtil](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/beans/DateUtil.groovy)

### Parsing a date in a string

```stringToDate``` expects a string with date in the simple format ```yyyy-MM-dd``` and returns Date instance:

```groovy
     Date date = DateUtil.stringToDate("2017-10-19")

     assert date
     assert date == new SimpleDateFormat("yyyy-MM-dd").parse("2017-10-19")
```

it's a shortcut for **convertStringToDateTime**

```groovy
     Date date = DateUtil.convertStringToDateTime("2017-10-19", "yyyy-MM-dd")

     assert date
     assert date == new SimpleDateFormat("yyyy-MM-dd").parse("2017-10-19")
```


### Converting Date instance to a string

```dateToJsonString``` converts a date to the format ```yyyy-MM-dd'T'HH:mm:ss.SSSZ```

```groovy
    Date date = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss').parse('2017-10-20 22:00:00')

    String result = DateUtil.dateToJsonString(date)
    assert result == date.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
```

it's a shortcut for **dateToString** method which accepts a format

```groovy
    Date date = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss').parse('2017-10-20 22:00:00')

    String result = DateUtil.dateToString(date, "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    assert result == date.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")


    result = DateUtil.dateToString(date)
    assert result == date.format("MM/dd/yyyy hh:mm:ss")
```


### Get the difference now and a specified date in hours

```groovy
    Calendar calendar = Calendar.getInstance()
    calendar.add(Calendar.HOUR, 1)
    calendar.add(Calendar.MINUTE, 30)

    assert 1L = DateUtil.getDateDifference_inHours(calendar.getTime())
```

### Get the difference between dates

We can calculate get number of months between two dates, for example:

```groovy
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd")
    Date date1 = format.parse("2017-10-19")
    Date date2 = format.parse("2017-12-19")

    2 == DateUtil.getMonthDiff(date1, date2)
```

or number of days

```groovy
    Date now = new Date()

    assert 0 == DateUtil.getDaysBetween(now, now)
    assert -10 == DateUtil.getDaysBetween(now - 10, now)
    assert  10 == DateUtil.getDaysBetween(now + 10, now)
```

## MultiFormatDateConverter

MultiFormatDateConverter extends general type conversion system for dates. It is used to date formatted string to date.

Under the hood it uses DateUtil and supports next formats "yyyy-MM-dd", "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd'T'HH:mm:ssZ",
"yyyy-MM-dd'T'HH:mm:ss", "MM/dd/yy"

To apply it to application add a spring bean for it. The bean name could be any

## Dealing with timezones when storing dates.

By default JDBC drivers stores and retrieves the dates/timestamps in local JVM timezone. 
However it is generally recommended to use UTC for storing dates in database. 

There are two ways to store the date values in UTC. One is to change the default time zone of JVM using ```TimeZone.setDefault( TimeZone.getTimeZone( "UTC" ) );```
However this forces to change the jvm default time zone which may not be possible in all cases.

Another option is to use the hibernate setting ```hibernamte.jdbc.time_zone```

With grails, the timezone which hibernate uses can be configured in ```application.yml``` as shown below.

```yaml
hibernate:
    jdbc:
      time_zone: UTC
``` 

This will instruct hibernate to store and retrieve the dates in UTC timezone.

However it should be noted that if you query the date values with JDBC it will be retrieved in JVM timezone and not UTC and will need to be converted to UTC manually or use the overloaded version of ResultSet.getTimeStamp that takes calendar as second argument
Eg. ```ResultSet#getTimestamp(int columnIndex, Calendar cal)```


[DateUtil]:https://yakworks.github.io/gorm-tools/api/gorm/tools/beans/DateUtil.html
