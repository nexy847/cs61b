package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.List;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // TODO: what if args is empty?
        //String firstArg = args[0];
        /*switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if(Repository.GITLET_DIR.exists()){
                    System.out.println("A Gitlet version-control system already exists in the current directory.");
                }
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                String fileName=args[1];
                File file=Utils.join(Repository.WORKINGCOPY,fileName);
                Repository.add(file);
                break;
            // TODO: FILL THE REST IN
        }*/
        //File file=Utils.join(Repository.CWD,"hello,wikit");
        //Repository.rm(file);
        /*List<Commit> commitList=Repository.BFSCommit();
        for(Commit commit:commitList){
            System.out.println(commit.getID());
        }*/
        File file=Utils.join(Repository.CWD,"hello,world");
        //Repository.commit("test rm and status");
        //Repository.rm(file);
        /*List<Commit> commitList= Repository.BFSCommit();
        Commit[] commitArray=Repository.HeapSort(commitList);
        for(Commit commit:commitArray){
            System.out.println(commit.getID());
        }*/
        //Repository.commit("test 1");
        //Repository.checkout("ccca03a5abbaa1d8e0a08be98e1f3f54a0eab878","hello,world",null);
        Repository.branch("hello");
    }
}
