## Gorm-tools provides a set of static utils which allow us to manipulate with dates much easier.

See [DateUtil](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/beans/DateUtil.groovy)


### Parsing a date in a string


**stringToDate** expects a string with date in the simple format "yyyy-MM-dd" and returns Date instance:

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

**dateToJsonString** converts a date to the format "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

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
