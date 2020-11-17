# Jdial

server port dial, java impl. coroutine support.

## quick start

```shell
// run default coroutine impl.
mvn compile dependency:properties exec:exec
```

```shell
// Output
port open: Optional[1080, 1086, 1087, 4300, 4301, 6942, 6943]
time elapse: 1818
```

## dependency

depend on [Quasar](https://docs.paralleluniverse.co/quasar/), use **Fibers** to boost performance.

## change arguments

```xml
// pom.xml

<argument>com.nickchenyx.util.Jdial</argument>
<argument>--hostname</argument>
<argument>localhost</argument>
<argument>--start-port</argument>
<argument>80</argument>
<argument>--end-port</argument>
<argument>9000</argument>
<argument>--timeout</argument>
<argument>200</argument>
```

There are different impl, such as single thread, multi thread. uncomment corresponding code line to run.

```java
// --  run fiber example
//        testSingleThread(hostname, startPort, endPort, timeout);
//        testMultiThread(hostname, startPort, endPort, timeout);
        testFiber(hostname, startPort, endPort, timeout);
```
```java
// --  run single thread example
        testSingleThread(hostname, startPort, endPort, timeout);
//        testMultiThread(hostname, startPort, endPort, timeout);
//         testFiber(hostname, startPort, endPort, timeout);
```
```java
// --  run multi thread example
//        testSingleThread(hostname, startPort, endPort, timeout);
        testMultiThread(hostname, startPort, endPort, timeout);
//        testFiber(hostname, startPort, endPort, timeout);
```

## improve project

issue me or email nickchenyx @ gmail dot com
