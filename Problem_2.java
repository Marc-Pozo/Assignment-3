// Marcelino Pozo
// Assignment 3 Problem 2
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Problem_2 {
    private static final int NUM_SENSORS = 8;
    private static final int NUM_READINGS_PER_HOUR = 60;

    private static final AtomicInteger[][] temperatureReadings = new AtomicInteger[NUM_SENSORS][NUM_READINGS_PER_HOUR];
    private static final Lock[][] locks = new Lock[NUM_SENSORS][NUM_READINGS_PER_HOUR];
    
    private static final ExecutorService sensorExecutor = Executors.newFixedThreadPool(NUM_SENSORS);

    public static void main(String[] args) 
    {
        initializeTemperatureReadings();

        // Start sensor threads
        for (int i = 0; i < NUM_SENSORS; i++) 
        {
            sensorExecutor.execute(new SensorThread(i));
        }

        // Start report compilation thread
        Report();

        // Shutdown executor services
        sensorExecutor.shutdown();
    }

    private static void Report() 
    {
        System.out.println("Hourly Report:");

        int[] low = new int[3],high = new int[3];
        int difference = 0;

        List<Integer> flattenedList = new ArrayList<>();
            
        // Simultaneously flattens the list and calculates the biggest difference within 10 minutes
        for (int row = 0; row < NUM_SENSORS; row++)
        {
            // Grab each row and then every number in each row (rows represent sensors, num is the time the sensor got the reading)
            // NOTE: This also means that differences in temp per 10 minutes are ONLY compared on a per thread basis.
            // I dont compare the reading of one thread to another threads reading 10 or less minutes later.
            for (int num = 0; num < NUM_READINGS_PER_HOUR; num++)
            {
                // Add to the flattened list
                int temp = temperatureReadings[row][num].get();   
                flattenedList.add(temp);
                    
                // Check the temp diferrence
                if(num == 0 || Math.abs(high[2] - low[2]) > 10) // We make sure the difference in time is no greater than 10
                {
                    // Store the temp, which sensor recorded it and at what time
                    high[0] = temp; high[1] = row; high[2] = num;                            
                    low[0] = temp; low[1] = row; low[2] = num;
                }
                else if (temp > high[0])
                {
                    high[0] = temp; high[1] = row; high[2] = num;
                } 
                else if (temp < low[0])
                {
                    low[0] = temp; low[1] = row; low[2] = num;
                }
            }

            // Check for the biggest temp difference
            if (row == 0 || Math.abs(high[0] - low[0]) > difference )
                difference  = Math.abs(high[0] - low[0]);
        }

        System.out.println("Biggest Difference within 10 Minutes: "+difference+"F");

        extremeTemps(flattenedList);
    }

    private static void extremeTemps(List<Integer> flattenedList)
    {
        // Sort the list in descending order
        Collections.sort(flattenedList, Collections.reverseOrder());
            
        System.out.println("Highest Temps:");
        for (int i = 0; i < 5; i++) 
        {
            System.out.println(flattenedList.get(i)+"F");
        }

        System.out.println("Lowest Temps:");
        for (int i = flattenedList.size() - 1; i > flattenedList.size() - 6; i--) 
        {
            System.out.println(flattenedList.get(i)+"F");
        }
    }

    private static void initializeTemperatureReadings() 
    {
        // Initializes the locks and Atomics
        for (int i = 0; i < NUM_SENSORS; i++) 
        {
            for (int j = 0; j < NUM_READINGS_PER_HOUR; j++) 
            {
                temperatureReadings[i][j] = new AtomicInteger();
                locks[i][j] = new ReentrantLock();
            }
        }
    }

    // Implementation of the sensors
    static class SensorThread implements Runnable 
    {
        private final int sensorId;
        private final Random random = new Random();

        // Constructor
        public SensorThread(int sensorId) 
        {
            this.sensorId = sensorId;
        }

        @Override
        public void run() {
            // Minute Loop
            for (int minute = 0; minute < NUM_READINGS_PER_HOUR; minute++) 
            {
                // Random temperature from -100F to 70F
                int temperature = random.nextInt(171) - 100;
                
                // Acquire Lock
                locks[sensorId][minute].lock();
                try{                    
                    temperatureReadings[sensorId][minute].set(temperature);
                }
                finally{
                    // Release Lock
                    locks[sensorId][minute].unlock();                 
                }
            }
        }
    }
}
