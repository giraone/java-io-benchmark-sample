package com.giraone.samples.io;

import org.openjdk.jmh.annotations.*;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A class for benchmarking different Java IO techniques
 * on a simple file copying task.
 *
 * Run with
 * <pre>
 *     mvn clean package
 *     java -jar target/benchmarks.jar
 * </pre>
 */

// 2 iterations to warm-up, that may last 5 seconds each
@Warmup(iterations=2, time=5, timeUnit=TimeUnit.SECONDS)
// 10 iterations to measure, that may last 10 seconds each
@Measurement(iterations=5, time=10, timeUnit=TimeUnit.SECONDS)
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

public class IoBenchmark {

    private final File srcFile = new File("testfiles/input-1000KB.jpg");
    private final File targetFile = new File("testfiles/output-1000KB.jpg");
    private final long fileSize = srcFile.length();

    // this shows, how we can perform four measure groups: 1 KByte, 4 KByte, 16 KByte, 64 KByte
    @Param({"1024", "4096", "16384", "65536"})
    public int bufferSize;

    @Benchmark
    public void copyFileUsingStreams() throws IOException {

        long bytesCopied = IoUtil.copyFileUsingStreams(srcFile, targetFile, bufferSize);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    public void copyFileUsingBufferedStreams() throws IOException {

        long bytesCopied = IoUtil.copyFileUsingBufferedStreams(srcFile, targetFile, bufferSize);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    public void copyFileUsingInChannelOutBufferedStream() throws IOException {

        long bytesCopied = IoUtil.copyFileUsingInChannelOutBufferedStream(srcFile, targetFile, bufferSize);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    public void copyFileUsingChannelWithDirectByteBuffer() throws IOException {

        long bytesCopied = IoUtil.copyFileUsingChannelWithDirectByteBuffer(srcFile, targetFile, bufferSize);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    public void copyFileUsingChannelTransferFrom() throws IOException {

        long bytesCopied = IoUtil.copyFileUsingChannelTransferFrom(srcFile, targetFile);
        assert bytesCopied == fileSize;
    }

    @Benchmark
    public void copyFileUsingChannelTransferTo() throws IOException {

        long bytesCopied = IoUtil.copyFileUsingChannelTransferTo(srcFile, targetFile);
        assert bytesCopied == fileSize;
    }
}
