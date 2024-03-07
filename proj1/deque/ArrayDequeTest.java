package deque;

import org.junit.Test;
import static org.junit.Assert.*;

public class ArrayDequeTest {

    @Test
    public void resizeExpandedFirst(){
        ArrayDeque<Integer> x=new ArrayDeque<>();
        for(int i=0;i<20;i++){
            x.addFirst(i);
        }
        x.printDeque();
    }

    @Test
    public void resizeExpandedLast(){
        ArrayDeque<Integer> x=new ArrayDeque<>();
        for(int i=0;i<20;i++){
            x.addLast(i);
        }
        x.printDeque();
    }

    @Test
    public void resizeShrink(){
        ArrayDeque<Integer> x=new ArrayDeque<>();
        for(int i=0;i<1000;i++){
            x.addLast(i);
        }

        for(int i=0;i<999;i++){
            x.removeLast();
        }

        x.printDeque();
        System.out.println(x.N);
        System.out.println(x.get(0));
    }
}
