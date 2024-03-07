package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE

    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> noResizing=new AListNoResizing<>();
        BuggyAList<Integer> buggyAList=new BuggyAList<>();
        for(int i=3;i<7;i++){
            noResizing.addLast(i);
            buggyAList.addLast(i);
        }

        for(int i=0;i<3;i++){
            int resize = noResizing.removeLast();
            int buggy = buggyAList.removeLast();
            assertEquals("wrong",resize,buggy);
        }
    }

    @Test
    public void randomTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> broken=new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                broken.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int sizeBroken= broken.size();
                System.out.println(sizeBroken);
                assertEquals(size,sizeBroken);
            } else if (operationNumber == 2) {
                //getLast && removeLast
                if (L.size() > 0){
                    int last=L.getLast();
                    int last1=L.removeLast();

                    int lastBroken= broken.getLast();
                    int last1Broken=broken.removeLast();
                    assertEquals(last,lastBroken);
                    assertEquals(last1,last1Broken);
                }
            }
        }
    }
}
