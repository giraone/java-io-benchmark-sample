# Benchmarking different Java IO/NIO techniques with JMH

This project shows

- a benchmark of different Java IO/NIO techniques on a simple file copying task of a 1 MByte file
- how to use [Java Microbenchmark Harness (JMH)](http://openjdk.java.net/projects/code-tools/jmh/) to do this

## Why?

*Simply for having this code online accessible for myself and for sharing with you.*

## What is tested?

The test compares copying using ...
- [java.io.InputStream](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html),
  [java.io.OutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html)
  and a `byte[]` buffer
- [java.io.BufferedInputStream](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedInputStream.html),
  [java.io.BufferedOutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedOutputStream.html)
  and a `byte[]` buffer
- [java.nio.channels.FileChannel](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html) for input
  together with [java.io.BufferedOutputStream](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedOutputStream.html) for output
  and a `byte[]` buffer
- [java.nio.channels.FileChannel](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html) and
  a *direct* [java.nio.ByteBuffer](https://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html)
- [java.nio.channels.FileChannel.transferFrom(ReadableByteChannel src, long position, long count)](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html#transferFrom-java.nio.channels.ReadableByteChannel-long-long-)
- [java.nio.channels.FileChannel.transferTo(long position, long count, WritableByteChannel target)](https://docs.oracle.com/javase/8/docs/api/java/nio/channels/FileChannel.html#transferTo-long-long-java.nio.channels.WritableByteChannel-)

## Code

All Java IO code is located in [IoUtil.java](src/main/java/com/giraone/samples/io/IoUtil.java).

The JMH benchmark code and its annotations is in [IoBenchmark.java](src/main/java/com/giraone/samples/io/IoBenchmark.java).
With the default JMH iteration settings, the test may take 10 to 20 minutes, so feel free to decrease the iterations
or reduced the number of buffer sizes, which are tested.

## Results

Here are the results from my notebook with Windows 8.1 x64, Intel i5, 1.80Ghz, an IDE disc and Oracle Java 8

### Benchmark with the different buffer sizes
```
Benchmark                                             (bufferSize)  Mode  Cnt   Score   Error  Units
IoBenchmark.copyFileUsingStreams                              1024  avgt    5  11.320 ± 0.744  ms/op
IoBenchmark.copyFileUsingStreams                              4096  avgt    5   6.258 ± 0.478  ms/op
IoBenchmark.copyFileUsingStreams                             16384  avgt    5   5.469 ± 0.612  ms/op
IoBenchmark.copyFileUsingStreams                             65536  avgt    5   5.118 ± 0.133  ms/op
IoBenchmark.copyFileUsingBufferedStreams                      1024  avgt    5   5.993 ± 0.809  ms/op
IoBenchmark.copyFileUsingBufferedStreams                      4096  avgt    5   5.881 ± 0.431  ms/op
IoBenchmark.copyFileUsingBufferedStreams                     16384  avgt    5   5.721 ± 0.286  ms/op
IoBenchmark.copyFileUsingBufferedStreams                     65536  avgt    5   5.333 ± 0.650  ms/op
IoBenchmark.copyFileUsingInChannelOutBufferedStream           1024  avgt    5   5.599 ± 0.482  ms/op
IoBenchmark.copyFileUsingInChannelOutBufferedStream           4096  avgt    5   5.528 ± 0.348  ms/op
IoBenchmark.copyFileUsingInChannelOutBufferedStream          16384  avgt    5   5.396 ± 0.229  ms/op
IoBenchmark.copyFileUsingInChannelOutBufferedStream          65536  avgt    5   5.132 ± 0.178  ms/op
IoBenchmark.copyFileUsingChannelWithDirectByteBuffer          1024  avgt    5  11.026 ± 0.497  ms/op
IoBenchmark.copyFileUsingChannelWithDirectByteBuffer          4096  avgt    5   6.112 ± 0.704  ms/op
IoBenchmark.copyFileUsingChannelWithDirectByteBuffer         16384  avgt    5   5.401 ± 0.860  ms/op
IoBenchmark.copyFileUsingChannelWithDirectByteBuffer         65536  avgt    5   5.194 ± 0.632  ms/op
IoBenchmark.copyFileUsingChannelTransferFrom                  1024  avgt    5   5.270 ± 0.080  ms/op
IoBenchmark.copyFileUsingChannelTransferFrom                  4096  avgt    5   5.361 ± 0.171  ms/op
IoBenchmark.copyFileUsingChannelTransferFrom                 16384  avgt    5   5.256 ± 0.083  ms/op
IoBenchmark.copyFileUsingChannelTransferFrom                 65536  avgt    5   5.272 ± 0.113  ms/op
IoBenchmark.copyFileUsingChannelTransferTo                    1024  avgt    5   5.253 ± 0.281  ms/op
IoBenchmark.copyFileUsingChannelTransferTo                    4096  avgt    5   5.285 ± 0.265  ms/op
IoBenchmark.copyFileUsingChannelTransferTo                   16384  avgt    5   5.179 ± 0.396  ms/op
IoBenchmark.copyFileUsingChannelTransferTo                   65536  avgt    5   5.275 ± 0.279  ms/op
```

## Build and run

```
mvn clean package
java -jar target/benchmarks.jar
```

## JMH settings

These are the JMH settings, defined using annotations:

```java
// 2 iterations to warm-up, that may last 5 seconds each
@Warmup(iterations=2, time=5, timeUnit=TimeUnit.SECONDS)
// 10 iterations to measure, that may last 10 seconds each
@Measurement(iterations=10, time=10, timeUnit=TimeUnit.SECONDS)
// Only one forked process
@Fork(1)
// Only one thread
@Threads(1)
// calculate the average time of one call
@BenchmarkMode(Mode.AverageTime)
// it ist enough to have this value in milliseconds
@OutputTimeUnit(TimeUnit.MILLISECONDS)
// use the same instance of this class for the whole benchmark, 
// so it is OK to have some fix member variables
@State(Scope.Benchmark)
```

A *cool* feature is the usage of a parameter group:

```
// this shows, how we can perform four measure groups: 1 KByte, 4 KByte, 16 KByte, 64 KByte
@Param({"1024", "4096", "16384", "65536"})
public int bufferSize;
```

## Open issues

The `FileChannel.transferFrom` and `FileChannel.transferTo` samples are independent of the buffer size given
by the JMH `@Param(...)` annotation, but they are repeated for each buffer size, which is unnecessary.

## More useful links

- JMH
  - [JMH tutorial by Jakob Jenkov](http://tutorials.jenkov.com/java-performance/jmh.html)

- Java IO and NIO
  - [Advanced Input & Output - Java Programming Tutorial](https://www.ntu.edu.sg/home/ehchua/programming/java/J5b_IO_advanced.html)
  - [Google Books: Cracking The Java Interviews (Java 8)](https://play.google.com/books/reader?id=O6fJBAAAQBAJ&hl=de&printsec=frontcover&pg=GBS.PA127)
