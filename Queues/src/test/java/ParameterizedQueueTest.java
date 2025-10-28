import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QueueInterface Conformance Tests")
public class ParameterizedQueueTest {

    // Providers for ANY queue implementation (bounded or unbounded)
    static Stream<Arguments> queueProviders() {
        return Stream.of(
                Arguments.of("ArrayBoundedQueue", (Supplier<QueueInterface<Integer>>) () -> new ArrayBoundedQueue<>(10)),
                Arguments.of("LinkedQueue", (Supplier<QueueInterface<Integer>>) LinkedQueue::new)
        );
    }

    @Nested
    @DisplayName("Common behavior (all implementations)")
    class CommonBehavior {
        @ParameterizedTest(name = "{0} — create empty")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_create(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            assertTrue(queue.isEmpty());
        }

        @ParameterizedTest(name = "{0} — enqueue")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_enqueue(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            queue.enqueue(42);
            assertFalse(queue.isEmpty());
            assertEquals(1, queue.size());
        }

        @ParameterizedTest(name = "{0} — dequeue")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_dequeue(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            queue.enqueue(99);
            Integer val = queue.dequeue();
            assertEquals(99, val);
            assertTrue(queue.isEmpty());
        }

        @ParameterizedTest(name = "{0} — isFull")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_isFull(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            queue.enqueue(1);
            queue.enqueue(2);
            assertFalse(queue.isFull());
            queue.enqueue(3);
            queue.enqueue(4);
            queue.enqueue(4);
            queue.enqueue(4);
            queue.enqueue(4);
            queue.enqueue(4);
            queue.enqueue(4);
            queue.enqueue(4);
            if (queue instanceof ArrayBoundedQueue) {
                assertTrue(queue.isFull());
            }
        }

        @ParameterizedTest(name = "{0} — isEmpty")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_isEmpty(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            assertTrue(queue.isEmpty());
            queue.enqueue(1);
            assertFalse(queue.isEmpty());
            queue.dequeue();
            assertTrue(queue.isEmpty());
        }

        @ParameterizedTest(name = "{0} — size")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_size(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            assertEquals(0, queue.size());
            queue.enqueue(1);
            assertEquals(1, queue.size());
            queue.enqueue(2);
            queue.enqueue(3);
            assertEquals(3, queue.size());
            queue.dequeue();
            assertEquals(2, queue.size());
        }

        @ParameterizedTest(name = "{0} — enqueue/dequeue")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_enqueue_dequeue(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            queue.enqueue(1);
            assertFalse(queue.isEmpty());
            Integer val = queue.dequeue();
            assertEquals(1, val);
            assertTrue(queue.isEmpty());
        }

        @ParameterizedTest(name = "{0} — underflow on empty dequeue")
        @MethodSource("ParameterizedQueueTest#queueProviders")
        void test_underflow(String name, Supplier<QueueInterface<Integer>> factory) {
            QueueInterface<Integer> queue = factory.get();
            assertThrows(QueueUnderflowException.class, queue::dequeue);
        }
    }

    @Nested
    @DisplayName("ArrayBoundedQueue-specific behavior")
    class ArrayBoundedQueueSpecific {
        @Test
        @DisplayName("ArrayBoundedQueue — overflow exception")
        void test_overflow() {
            ArrayBoundedQueue<Integer> queue = new ArrayBoundedQueue<>(2);
            queue.enqueue(1);
            queue.enqueue(2);
            assertThrows(QueueOverflowException.class, () -> queue.enqueue(3));
        }
    }

    @Nested
    @DisplayName("LinkedQueue-specific behavior")
    class LinkedQueueSpecific {
        @Test
        @DisplayName("LinkedQueue — never full (unbounded)")
        void test_neverFull() {
            LinkedQueue<Integer> queue = new LinkedQueue<>();
            assertFalse(queue.isFull());
            // Add many elements - LinkedQueue should never be full
            for (int i = 0; i < 1000; i++) {
                queue.enqueue(i);
                assertFalse(queue.isFull());
            }
            assertEquals(1000, queue.size());
        }
        
        @Test
        @DisplayName("LinkedQueue — large queue operations")
        void test_largeQueue() {
            LinkedQueue<Integer> queue = new LinkedQueue<>();
            // Test that LinkedQueue can handle large number of elements
            int largeSize = 10000;
            for (int i = 0; i < largeSize; i++) {
                queue.enqueue(i);
            }
            assertEquals(largeSize, queue.size());
            
            // Verify FIFO order
            for (int i = 0; i < largeSize; i++) {
                assertEquals(i, queue.dequeue());
            }
            assertTrue(queue.isEmpty());
        }
        
        @Test
        @DisplayName("LinkedQueue — alternating enqueue/dequeue")
        void test_alternatingOperations() {
            LinkedQueue<Integer> queue = new LinkedQueue<>();
            // Test alternating operations maintain correct state
            for (int i = 0; i < 100; i++) {
                queue.enqueue(i);
                Integer val = queue.dequeue();
                assertEquals(i, val);
                assertTrue(queue.isEmpty());
            }
            // Queue should be empty after all operations
            assertTrue(queue.isEmpty());
        }
    }
}
