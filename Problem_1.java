// Marcelino Pozo
// Assignment 3 Problem 1
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Problem_1 
{
    private static Linked_List list = new Linked_List();     
    private static final ExecutorService helperExecutor = Executors.newFixedThreadPool(4);

    static int giftCardCount;
    static Lock lock = new ReentrantLock();
    
    // Code for each the thread
    static class HelperThread implements Runnable 
    {
        int count;
        private ArrayList<Integer> myBag = new ArrayList<Integer>();

        // Constructor
        public HelperThread(ArrayList<Integer> bag)
        {
            this.myBag = bag;
            this.count = 0;
        }

        private void Choice()
        {
            // Picks a random number and makes a choice
            Random rand = new Random(); 
            for (int i = 0; i < myBag.size(); i++) 
            {
                int num = rand.nextInt(2);

                int temp = myBag.get(i);

                if (num == 0)
                {
                    // Add to the list                
                    list.insert(temp);                    
                }
                else if (num == 1)
                {
                    // Remove from the list
                    if(list.remove(temp))
                    {
                        myBag.remove(i);
                        lock.lock();
                        try {
                            giftCardCount++;
                        } finally {
                            lock.unlock();
                        }
                    }
                }
                else if (num == 2)
                {
                    // Search through the list
                    list.search(temp);
                }
            }                      
        }

        // The function that runs when the thread starts
        @Override
        public void run() 
        {
            // The helpers add to remove from or check the list    
            do {
                Choice();                
            } while (!myBag.isEmpty());

            System.out.println("Giftcard Count:"+giftCardCount);
        }
    }
    
    // Present (node) class
    static class Present
    {
        Integer  id;
        Present next;
        Lock lock;

        public Present(Integer id) 
        {
            this.id = id;
            this.next = null;
            this.lock = new ReentrantLock();
        }
    }
    
    static class Linked_List
    {
        // Head node
        private Present head;

        // Constructor
        public Linked_List() 
        {
            this.head = new Present(Integer.MIN_VALUE);
            head.next = new Present(Integer.MAX_VALUE);
        }

        // Method to insert a new node based on id
        public void insert(int id) 
        {        
            head.lock.lock();
            Present pred = head;           
            if (id == pred.id)
                return;
            try {
                Present current = pred.next;
                // The list has at least 2 non null values
                current.lock.lock();
                // Insertion in the middle or end of list
                try {
                    while (current.id < id) 
                    {
                        pred.lock.unlock();
                        pred = current;
                        current = current.next;
                        current.lock.lock();    
                    }
                    if (current.id == id)
                        return;
                                
                    Present newPresent = new Present(id);
        
                    if (id < current.id)
                    {                    
                        newPresent.next = current;
                        pred.next = newPresent;
                    }
                    else
                        current.next = newPresent;
        
                    return;
                }
                finally {
                    current.lock.unlock();                               
                }            
            }
            finally {
                pred.lock.unlock();
            }
        }

        // Removes a present from the head and writes a thank you card
        public boolean remove(int id)
        {
            Present pred = null, curr = null;
            head.lock.lock();
            // Tries removing the present with the id
            try {
                pred = head;
                curr = pred.next;
                curr.lock.lock();
                try {
                    while( curr.id < id)
                    {
                        pred.lock.unlock();
                        pred = curr;
                        curr = curr.next;
                        curr.lock.lock();
                    }
                    if(curr.id == id)
                    {
                        pred.next = curr.next;
                        return true;
                    }
                    return false;
                }
                finally {
                    curr.lock.unlock();
                }
            }
            finally {
                pred.lock.unlock();
            }
        }

        // Searches sequentially through the list and returns true if the id is found
        public boolean search(int id)
        {
            Present current = head;

            head.lock.lock();

            try {
                while (current != null) 
                {
                    current.lock.lock();
                    try {                        
                        if(current.id == id)
                            return true;
                    }
                    finally {
                        current.lock.unlock();
                    }
                    current = current.next;
                }
            }
            finally {
                head.lock.unlock();
            }
            
            return false;
        }
        
        // Method to display the contents of the linked list
        public int display() 
        {
            int count = 0;
            Present current = head;
            while (current != null)
            {
                //System.out.print(current.id + " ");
                current = current.next;
                count++;
            }
            //System.out.println("Count "+count);
            return count;
        }
    }
    
    public static void main(String[] args) 
    {
        giftCardCount = 0;
        Random rand = new Random();       

        // Execute the same 4 threads with 1000 numbers per execution and add extra lists of numbers to the queue
        for (int k = 0; k < 500; k++) 
        {
            // Create a new list to hold the random numbers for each iteration
            ArrayList<Integer> bagOfGifts = new ArrayList<>();
            
            // Adds a random nums to the bag
            for (int j = 0; j < 1000; j++) 
            {
                bagOfGifts.add(rand.nextInt());
            }
            
            // Execute the HelperThread
            helperExecutor.execute(new HelperThread(bagOfGifts));
        }
        // Shutdown the threads
        helperExecutor.shutdown();
    }
}