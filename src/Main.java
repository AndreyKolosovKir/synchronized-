import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static final Integer N = 1000;
    public static final Map<Integer, Integer> sizeToFreq = new HashMap<>();

    public static void main(String[] args) {
        List<Future> futureList = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(N);

        for (int i = 0; i < N; i++) {
            futureList.add(executorService.submit(new CallableTask()));
        }

        new Thread(() -> {
            for (int k = 0; k < N; k++) {
                synchronized (sizeToFreq) {
                    {
                        int x = 0;
                        try {
                            x = (int) futureList.get(k).get();
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                        if (sizeToFreq.get(x) == null) {
                            sizeToFreq.put(x, 1);
                        } else if (sizeToFreq.get(x) != null) {
                            sizeToFreq.put(x, sizeToFreq.get(x) + 1);
                        }
                        sizeToFreq.notify();
                    }
                }
            }
        }).start();


        new Thread(() -> {
            int max = 0;
            int repeats = 0;
            synchronized (sizeToFreq) {
                try {
                    sizeToFreq.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                try {
                    for (Map.Entry<Integer, Integer> count : sizeToFreq.entrySet()) {
                        if (count.getValue() > repeats) {
                            max = count.getKey();
                            repeats = count.getValue();
                        }
                    }
                    System.out.println("Самое частое количество повторений - " + max + " (встретилось " + repeats + " раз)" +
                            "\nДругие размеры:\n");
                    for (Map.Entry<Integer, Integer> count : sizeToFreq.entrySet()) {
                        if (count.getValue() != repeats) {
                            System.out.format(" %d (%d раз)\n", count.getKey(), count.getValue());
                        }
                    }
                } catch (NullPointerException e) {
                    System.out.println(e);
                }
            }

        }).start();


        executorService.shutdown();
    }


    public static String generateRoute(String letters, int length) {
        Random random = new Random();
        StringBuilder route = new StringBuilder();
        for (int i = 0; i < length; i++) {
            route.append(letters.charAt(random.nextInt(letters.length())));
        }
        return route.toString();
    }

    private static class CallableTask implements Callable {
        @Override
        public Integer call() {
            String text = generateRoute("RLRFR", 100);
            int countR = 0;
            for (char elementR : text.toCharArray()) {
                if (elementR == 'R') {
                    countR++;
                }
            }
            return countR;
        }
    }
}