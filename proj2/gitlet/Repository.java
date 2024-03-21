package gitlet;

import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = Utils.join(CWD, ".gitlet");
    public static final File ADDSTAGE=Utils.join(GITLET_DIR,"addStage");
    public static final File REMOVESTAGE=Utils.join(GITLET_DIR,"removeStage");
    public static final File OBJECTS=Utils.join(GITLET_DIR,"objects");
    public static final File REFS=Utils.join(GITLET_DIR,"refs");
    public static final File HEADS=Utils.join(REFS,"heads");
    public static final File HEAD=Utils.join(GITLET_DIR,"HEAD");
    public static final File WORKINGCOPY=Utils.join(CWD,"gitlet");

    /* TODO: fill in the rest of this class. */

    public static void init() throws IOException {
        String message="initial commit";
        Commit commit=new Commit(message);
        String commitHash=Utils.sha1(Utils.serialize(commit));
        GITLET_DIR.mkdir();
        OBJECTS.mkdir();
        //File refs=Utils.join(GITLET_DIR,"refs");
        //refs.mkdir();
        REFS.mkdir();
        //File heads=Utils.join(refs,"heads");
        //heads.mkdir();
        HEADS.mkdir();
        File master=Utils.join(HEADS,"master");
        master.createNewFile();
        Utils.writeContents(master,commitHash);
        //File HEAD=Utils.join(GITLET_DIR,"HEAD");
        HEAD.createNewFile();
        Utils.writeContents(HEAD,commitHash);
        ADDSTAGE.createNewFile();
        REMOVESTAGE.createNewFile();
    }

    public static void add(File file) throws IOException{
        if(!isExist(file)){
            System.out.println("File does not exist.");
            System.exit(0);
        }
        if(OBJECTS.listFiles().length==0){
            addFile(file);
        }
        if(Utils.readContents(ADDSTAGE).length==0){
            if(!isDuplicatedCommit(file)&&!isDeleted(file)){
                addFile(file);
            }
        }else {
            Stage stage = Utils.readObject(ADDSTAGE, Stage.class);
            if (!isDuplicatedCommit(file) && !isDeleted(file) &&
                    !stage.isDuplicated(file)) {
                addFile(file);
            }
        }

    }

    private static boolean isExist(File file){
        return file.exists();
    }

    //judge 最新commit的文件hash
    private static boolean isDuplicatedCommit(File file){
        //File commitFile=Utils.join(GITLET_DIR,"refs","heads","master");
        //File objects=Utils.join(...,"objects");
        File[] blobFiles=OBJECTS.listFiles();
        for(File blobFile:blobFiles){
            //如果blobFile的文件名是最新commit的哈希值,就从这个blob文件中取出commit对象
            if(blobFile.getName().equals(Utils.readContentsAsString(HEAD))){
                Commit commit=Utils.readObject(blobFile,Commit.class);
                //判断以文件本身的哈希为值的映射是否存在
                if(commit.getPathToBlobID().containsValue(Utils.sha1(Utils.serialize(file)))){
                    return true;
                }
            }
        }
        return false;
    }

    //是否已删除(removeStage内)
    private static boolean isDeleted(File file){
        if(Utils.readContents(REMOVESTAGE).length==0){
            return false;
        }
        //File removeStageFile=Utils.join(GITLET_DIR,"removeStage");
        Stage removeStage=Utils.readObject(REMOVESTAGE,Stage.class);
        Map<String,String> pathToBlobID=removeStage.getPathToBlobID();
        if(pathToBlobID.containsValue(Utils.sha1(Utils.serialize(file)))){
            return true;
        }
        return false;
    }

    private static void addFile(File file) throws IOException {
        Stage stage=new Stage();
        //file=>File file=Utils.join(WORKINGCOPY,file.getName);
        stage.addFile(file.getPath(),Utils.sha1(Utils.serialize(file)));
        stage.saveAddFile();
        Blob blob=new Blob();
        blob.addFile(file.getPath(),file);
        blob.setID();
        blob.saveFile();
    }

    public static void commit(String message) throws IOException {
        if(!isAnyFileInAddStage()){
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        List<String> parentID=new ArrayList<>();
        parentID.add(findParentID());
        Commit newCommit = new Commit(message);
        newCommit.setTimeStamp(findParentID());
        newCommit.setParentID(parentID);
        Map<String,String> newPathToBlobID=new TreeMap<>();
        if(findPathToBlobIDInParent(findParentID())==null){
            newPathToBlobID=findPathToBlobIDInStage();
        }else {
            Map<String, String> newPathToBlobID1 = fliterPathToBlobID(findPathToBlobIDInParent(findParentID()),
                    findPathToBlobIDInStage());
            Map<String, String> newPathToBlobID2 = distinctMap(findPathToBlobIDInParent(findParentID()),
                    findPathToBlobIDInStage());
            newPathToBlobID = removeAddMap(newPathToBlobID1, newPathToBlobID2);
        }
        newCommit.setPathToBlobID(newPathToBlobID);
        newCommit.setFileName(findFileName(newPathToBlobID));
        newCommit.setID();
        newCommit.saveCommit();
        Utils.writeContents(HEAD,newCommit.getID());
        emptyAddStage();
        File master=Utils.join(HEADS,"master");
        Utils.writeContents(master,newCommit.getID());
    }

    private static boolean isAnyFileInAddStage(){
        if(Utils.readContents(ADDSTAGE).length>0){
            return true;
        }else{
            return false;
        }
    }

    private static List<String> findFileName(Map<String,String> newPathToBlobID){
        List<String> filesName=new ArrayList<>();
        Set<String> keys=newPathToBlobID.keySet();
        for(String key:keys){
            File objects=Utils.join(key);
            filesName.add(objects.getName());
        }
        return filesName;
    }

    private static String findParentID(){
        return Utils.readContentsAsString(HEAD);
    }

    private static Map<String,String> findPathToBlobIDInStage(){
        Stage addStage=Utils.readObject(ADDSTAGE,Stage.class);
        return addStage.getPathToBlobID();
    }

    private static Map<String,String> findPathToBlobIDInParent(String parentID){
        File[] files=OBJECTS.listFiles();
        for(File file:files){
            if(parentID.equals(file.getName())){
                Commit parent=Utils.readObject(file,Commit.class);
                return parent.getPathToBlobID();
            }
        }
        return null;
    }

    private static Map<String,String> fliterPathToBlobID(Map<String,String> parent,Map<String,String> addStage){
        Map<String,String> newCommit=new TreeMap<>();
        newCommit.putAll(notNullMap(parent));
        newCommit.putAll(notNullMap(addStage));
        return newCommit;
    }

    private static Map<String,String> notNullMap(Map<String,String> commit){//非空,处理文件名不同但哈希值相同的问题
        Map<String,String> newTemp=new TreeMap<>();
        Set<String> pathold=commit.keySet();
        for(String path:pathold){
            File old=Utils.join(path);
            if(old.exists()){
                newTemp.put(path,commit.get(path));
            }else{
                continue;
            }
        }
        return newTemp;
    }

    //处理文件名相同但内容不同的问题(oldCommit和addStage),仅保存addStage的文件
    private static Map<String,String> distinctMap(Map<String,String> parent,Map<String,String> addStage){
        Map<String,String> newTemp=new TreeMap<>();
        Set<String> parentKeys=parent.keySet();
        Set<String> addStageKeys=addStage.keySet();
        outer:
        for(String parentKey:parentKeys){
            for(String addStageKey:addStageKeys){
                if(parentKey.equals(addStageKey)){
                    newTemp.put(addStageKey,addStage.get(addStageKey));
                }
            }
        }
        return newTemp;
    }

    private static Map<String,String> removeAddMap(Map<String,String> old,Map<String,String> add){
        Set<String> addKeys=add.keySet();
        Set<String> oldKeys=old.keySet();
        for(String addKey:addKeys){
            for(String oldKey:oldKeys){
                if(addKey.equals(oldKey)){
                    old.remove(oldKey);
                }
            }
            old.put(addKey,add.get(addKey));
        }
        return old;
    }

    private static void emptyAddStage() throws IOException {
        ADDSTAGE.delete();
        if(!ADDSTAGE.exists()){
            ADDSTAGE.createNewFile();
        }
    }

    public static void rm(File file){
        if(justOnAddStage(file)){
            Stage addStage=Utils.readObject(file,Stage.class);
            Set<String> keys=addStage.getPathToBlobID().keySet();
            for(String key:keys){
                if(key.equals(file.getPath())){
                    addStage.getPathToBlobID().remove(key);
                }
            }
        }else if(onCommitNotExists(file)){
            File[] files=OBJECTS.listFiles();
            for(File fileCommit:files){
                if(fileCommit.getName().equals(Utils.readContentsAsString(HEAD))){
                    Commit commit=Utils.readObject(fileCommit,Commit.class);
                    Set<String> keys=commit.getPathToBlobID().keySet();
                    for(String key:keys){
                        if(key.equals(Utils.sha1(Utils.serialize(file)))){
                            Stage removeStage=new Stage();
                            removeStage.addRemoveFile(key,commit.getPathToBlobID().get(key));
                            Utils.writeObject(REMOVESTAGE,removeStage);
                        }
                    }
                }
            }
        }else if(onCommitExists(file)){
            File[] files=OBJECTS.listFiles();
            for(File fileCommit:files){
                if(fileCommit.getName().equals(Utils.readContentsAsString(HEAD))){
                    Commit commit=Utils.readObject(fileCommit,Commit.class);
                    Set<String> keys=commit.getPathToBlobID().keySet();
                    for(String key:keys){
                        if(key.equals(file.getPath())){
                            Stage removeStage=new Stage();
                            removeStage.addRemoveFile(key,commit.getPathToBlobID().get(key));
                            Utils.writeObject(REMOVESTAGE,removeStage);
                        }
                    }
                }
            }
            Utils.restrictedDelete(file);
        }else{
            System.out.println("No reason to remove the file.");
        }
    }

    private static boolean justOnAddStage(File file){
        if(!isDuplicatedCommit(file)){
            if(Utils.readContents(ADDSTAGE).length==0){
                return false;
            }else{
                Stage addStage=Utils.readObject(ADDSTAGE,Stage.class);
                if(addStage.isDuplicated(file)){
                    return true;
                }else{
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean onCommitNotExists(File file){
        if(isDuplicatedCommit(file)){
            if(!isExist(file)){
                return true;
            }else return false;
        }
        return false;
    }

    private static boolean onCommitExists(File file){
        if(isDuplicatedCommit(file)){
            if(isExist(file)) return true;
            else return false;
        }
        return false;
    }

    public static void log(){//图的广度遍历,后做堆排序

    }

    public static List<Commit> BFSCommit(){//对图做广度遍历,得到一个Commit的List序列
        List<Commit> commitList=new ArrayList<>();
        String head=Utils.readContentsAsString(HEAD);
        File[] files=OBJECTS.listFiles();
        Commit heads=new Commit("nima");
        for(File file:files){
            if(file.getName().equals(Utils.readContentsAsString(HEAD))){
                Commit headf=Utils.readObject(file,Commit.class);
                heads=headf;
                break;
            }
        }
        List<String> parents=heads.getParentID();
        Set<Commit> visited=new HashSet<>();
        visited.add(heads);
        Queue<Commit> que=new LinkedList<>();
        que.offer(heads);
        while(!que.isEmpty()){
            Commit vet=que.poll();
            commitList.add(vet);
            //遍历邻接顶点
            outer:
            for(String parent:parents){
                for(File file:files){
                    if(file.getName().equals(parent)){
                        Commit commit=Utils.readObject(file,Commit.class);
                        if(visited.contains(commit)) continue outer;
                        que.offer(commit);
                        visited.add(commit);
                    }
                }
            }
            parents=vet.getParentID();
        }
        return commitList;
    }

    /*private static List<Commit>  HeapSort(List<Commit> commitList){

    }*/
}
