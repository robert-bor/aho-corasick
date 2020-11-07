package org.ahocorasick.benchmark;

import com.intendia.jmh.GcProfiler;
import org.ahocorasick.trie.PayloadEmit;
import org.ahocorasick.trie.PayloadTrie;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.CommandLineOptionException;
import org.openjdk.jmh.runner.options.CommandLineOptions;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


@State(Scope.Benchmark)
@BenchmarkMode({Mode.Throughput})
@Warmup(iterations = 3, time = 3)
@Measurement(iterations = 5, time = 3)
@Timeout(time = 30)
@Threads(1)
@Fork(1)
public class BenchmarkPayloadTrie {

    @State(Scope.Benchmark)
    public static class BenchmarkState {
        @Param({"10", "100", "1000", "10000"})
        public int size;

        public List<String> keywords;

        public String input1;
        public String input2;
        public String input3;

        @Setup
        public void setup() {
            Random random = new Random(42L);
            this.keywords = IntStream.range(0, this.size)
                    .boxed()
                    .map(index -> randomString(random))
                    .collect(Collectors.toList());
            this.input1 = this.keywords.get(0) + this.keywords.get(1) + this.keywords.get(3);
            this.input2 = this.keywords.get(3) + " " + this.keywords.get(1) + " " + this.keywords.get(0) + "#";
            this.input3 = randomString(random, 30)+" gggggggggg gfgfgf 4324324sd dse31232132 324324325434";
        }
    }

    @State(Scope.Benchmark)
    public static class SimpleState {
        public BenchmarkState state;
        public PayloadTrie<String> trie;

        @Setup
        public void setup(BenchmarkState state) {
            this.state = state;
            PayloadTrie.PayloadTrieBuilder<String> trieBuilder = PayloadTrie.builder();
            state.keywords.forEach(trieBuilder::addKeyword);
            this.trie = trieBuilder.build();
        }
    }

    @State(Scope.Benchmark)
    public static class WholeWordState {
        public BenchmarkState state;
        public PayloadTrie<String> trie;

        @Setup
        public void setup(BenchmarkState state) {
            this.state = state;
            PayloadTrie.PayloadTrieBuilder<String> trieBuilder = PayloadTrie.<String>builder().onlyWholeWords();
            state.keywords.forEach(trieBuilder::addKeyword);
            this.trie = trieBuilder.build();
        }
    }

     @Benchmark
     public void negativeSimple(Blackhole blackhole, SimpleState state) {
         Collection<PayloadEmit<String>> result = state.trie.parseText(state.state.input3);
         blackhole.consume(result);
     }

    // @Benchmark
    // public void negativeWw(Blackhole blackhole, WholeWordState state) {
    //     Collection<PayloadEmit<String>> result = state.trie.parseText(state.state.input3);
    //     blackhole.consume(result);
    // }

    @Benchmark
    public void positve1Simple(Blackhole blackhole, SimpleState state) {
        Collection<PayloadEmit<String>> result = state.trie.parseText(state.state.input1);
        blackhole.consume(result);
    }

    // @Benchmark
    // public void positve1Ww(Blackhole blackhole, WholeWordState state) {
    //     Collection<PayloadEmit<String>> result = state.trie.parseText(state.state.input1);
    //     blackhole.consume(result);
    // }

    @Benchmark
    public void positve2Simple(Blackhole blackhole, SimpleState state) {
        Collection<PayloadEmit<String>> result = state.trie.parseText(state.state.input2);
        blackhole.consume(result);
    }

    // @Benchmark
    // public void positve2Ww(Blackhole blackhole, WholeWordState state) {
    //     Collection<PayloadEmit<String>> result = state.trie.parseText(state.state.input2);
    //     blackhole.consume(result);
    // }

    private static String randomString(Random random) {
        return randomString(random, 5);
    }

    private static String randomString(Random random, int length) {
        int leftLimit = 223; // Latin Small Letter sharp S
        int rightLimit = 246; // Latin Small Letter O with diaeresis

        return random.ints(leftLimit, rightLimit + 1)
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static void main(String[] args) throws CommandLineOptionException, RunnerException {
        Options opt = new OptionsBuilder()
                .parent(new CommandLineOptions(args))
                .include(Benchmark.class.getSimpleName())
                .addProfiler(GcProfiler.class)
                .build();

        new org.openjdk.jmh.runner.Runner(opt).run();
    }
}
