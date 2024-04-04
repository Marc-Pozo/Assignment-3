Marcelino Pozo

Problem 1 Report:
    - Efficiency:
        Timing problem 1 it takes around 23-25 seconds to fully run, this is on an i5-13400 with 500000 presents.
        What helped me drop the time from 3-5 minutes to this time was splitting the arraylists into lists of about 1000 and then running 500 of them in a queue.

    - Correctness:
        I have checked my linked list functions for correctness in a single threaded environment numerous times, not once did it insert out of order or fail to remove
        an existing number from the list. Also I tested concurrency with smaller batches of numbers which I would then print to prove that the code was correctly locking and
        unlocking the presents (nodes).

    - Experimental Evaluation:
        The experimental value of this problem is found in the linked list implementation. I believe this is the most important part of this problem because it proves how much faster
        a linked list can perform when parallelized. There are issues when dealing with testing (as I found through the coding process) but those can be overcome by testing little 
        by little.

Problem 2 Report:
    - Efficiency:
        This problem is implemented very efficiently. As I was coding up the solution I tried my hardest to avoid repeat operations if they were unnnecessary. For example I only 
        begin calculates on the top or bottom 5 temps after I get all the data. I also simultaneously flatten the list while grabbing the biggest difference in temp.
    - Correctness:
        I have evaluated the correctness by looking at the reports data as well as printing and then manually sorting the data to verify the top and bottom 5 temps. Furthermore,
        I follow the rubric by correctly using 8 concurrent threads, each filling up an array with random tempature data between -100 and 70F.
    - Experimental Evaluation
        In conclusion I have found that using 8 concurrent threads to take in data can be tricky when trying to store in the same array, it requires careful locking and unlocking so
        as not to overwrite previous results.