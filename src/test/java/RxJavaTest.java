import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import org.junit.Test;

public class RxJavaTest {

    @Test
    public void executionOrder_alsoItsLazy() {
        System.out.println("0. before");

        Flowable.fromArray("hello", "hi there", "i'm not a string")
            .doOnNext(s -> System.out.println("1. " + s))
            .filter(s -> s.contains(" "))
            .doOnNext(s -> System.out.println("2. " + s))
            .subscribe();

        System.out.println("3. after");
//        0. before
//        1. hello
//        1. hi there
//        2. hi there
//        1. i'm not a string
//        2. i'm not a string
//        3. after
    }

    @Test
    public void itsReallyLazy() {
        System.out.println("0. before");

        Flowable.fromArray("hello", "hi there", "i'm not a string")
            .doOnNext(s -> System.out.println("1. " + s))
            .filter(s -> s.contains(" "))
            .doOnNext(s -> System.out.println("2. " + s))
            .firstOrError()
            .subscribe(s -> System.out.println("result: " + s));

        System.out.println("3. after");

//        0. before
//        1. hello
//        1. hi there
//        2. hi there
//        result: hi there
//        3. after
    }

    @Test
    public void streamsAreImmutable_andReusable() {
        System.out.println("0. before");

        Flowable<String> original = Flowable
            .fromArray("hello", "hi there", "i'm not a string")
            .doOnNext(s -> System.out.println("1. " + s))
            .filter(s -> s.contains(" "))
            .doOnNext(s -> System.out.println("2. " + s));

        original
            .toList()
            .subscribe(s -> System.out.println("result 1: " + s));

//        0. before
//        1. hello
//        1. hi there
//        2. hi there
//        1. im not...
//        2. im not...
//        result 1: [hi there, im not...]
//        3. after

        original
            .map(s -> s.length()) // Flowable<Integer>
            .subscribe(s -> System.out.println("result 2: " + s));

        //        0. before
//        1. hello
//        1. hi there
//        2. hi there
//        1. im not...
//        2. im not...
//        result 2: hi there
//        result 2: im not...
//        3. after

        System.out.println("3. after");
    }

    @Test
    public void RPC() {
        Queue<Callable<String>> actions = new LinkedList<>();
        actions.add(() -> {
            throw new IllegalStateException("I just don't know what that is...");
        });
        actions.add(() -> "i did find the thing");

        Flowable.fromCallable(() -> actions.poll())
            .doOnNext(action -> System.out.println("1. about to run: " + action))
            .flatMapSingle(action -> Single.fromCallable(() -> action.call()))
            .retry(2)
            .onErrorReturn(__ -> "default")
            .firstOrError()
            .doOnSuccess(res -> System.out.println("2. result: " + res))
            .subscribe(
                res -> System.out.println("success " + res),
                err -> System.err.println("error " + err)
            );
    }
}