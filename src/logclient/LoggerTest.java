package logclient;

//import org.junit.Test;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;

public class LoggerTest {

    @Test
    public void defaultLoggingg() throws InterruptedException, ExecutionException {
        final LogClient logClient = new LoggerImplementation(10);
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        logClient.start("1", 1);
        logClient.start("2", 2);
        logClient.start("3", 3);
        logClient.end("3");
        logClient.end("2");
        tasks.add(runAsync(logClient::poll));
        tasks.add(runAsync(logClient::poll));
        logClient.end("1");
        tasks.add(runAsync(logClient::poll));
        allOf(tasks.toArray(new CompletableFuture[0])).get();
    }

    @Test
    public void concurrencyTest() throws InterruptedException, ExecutionException {
        final LogClient logClient = new LoggerImplementation(10);
        final int size = 1000;
        final ExecutorService executorService = Executors.newFixedThreadPool(size);
        final Random random = new Random();
        final List<String> commands = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            commands.add("POLL");
            commands.add("END " + i);
        }
        Collections.shuffle(commands);
        Map<Integer, Integer> ends = new HashMap<>();
        for (int i = 0; i < size * 2; i++) {
            if (!commands.get(i).equals("POLL")) {
                ends.put(Integer.parseInt(commands.get(i).split(" ")[1]), i);
            }
        }
        int index = commands.size() - 1;
        while (index >= 0) {
            if (commands.get(index).startsWith("END ")) {
                final int taskId = Integer.parseInt(commands.get(index).split(" ")[1]);
                final int insertionPoint = random.nextInt(Math.min(ends.get(taskId), ends.getOrDefault(taskId + 1, commands.size() - 1)) + 1);
                commands.add(insertionPoint, "START " + taskId);
                if (insertionPoint <= index) {
                    index++;
                }
            }
            index--;
        }
        final List<CompletableFuture<Void>> tasks = new CopyOnWriteArrayList<>();
        for (final String command : commands) {
            if (command.equals("POLL")) {
                tasks.add(runAsync(logClient::poll, executorService));
            } else {
                final String id = command.split(" ")[1];
                if (command.startsWith("START ")) {
                    logClient.start(id, size - Long.parseLong(id) + 1);
                } else {
                    logClient.end(id);
                }
            }
        }
        allOf(tasks.toArray(new CompletableFuture[0])).get();
        // allOf(tasks.toArray(CompletableFuture[]::new)).get();

    }
}
