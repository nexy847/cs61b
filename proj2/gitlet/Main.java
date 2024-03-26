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
        File file=Utils.join(Repository.CWD,"bothof");
        File file1=Utils.join(Repository.CWD,"only_master");
        //Repository.commit("test rm and status");
        //Repository.rm(file);
        /*List<Commit> commitList= Repository.BFSCommit();
        Commit[] commitArray=Repository.HeapSort(commitList);
        for(Commit commit:commitArray){
            System.out.println(commit.getID());
        }*/
        //Repository.commit("master branch commit:add a file that only master");
        //Repository.checkout(null,null,"master");
        Repository.log();
        Repository.status();
        //Repository.commit("master branch commit file didn't exists in hello branch");
        Commit currentCommit=Repository.findCommitByCommitID("006ab65b8daec1802eb9fb179330629eb93457fb");
        Commit givenCommit=Repository.findCommitByCommitID("a8c8795aed3940d2a19d5d8a1fcf4cf7ad4a6ce9");
        Commit splitPoint=Repository.findCommitByCommitID("04a5cd1a79b6ac414b856d338f02d8ca26e5c3e6");
        System.out.println(Repository.isModifiedByCurrent(splitPoint,currentCommit,file));
        System.out.println(Repository.isModifiedByGiven(splitPoint,givenCommit,file));
        Repository.merge("hello");
    }
}
