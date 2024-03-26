package gitlet;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
                if(commit.getPathToBlobID().containsValue(Utils.sha1(Utils.readContents(file)))){
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
        if(pathToBlobID.containsValue(Utils.sha1(Utils.readContents(file)))){
            return true;
        }
        return false;
    }

    private static void addFile(File file) throws IOException {
        Stage stage=new Stage();
        //file=>File file=Utils.join(WORKINGCOPY,file.getName);
        stage.addFile(file.getPath(),Utils.sha1(Utils.readContents(file)));
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
        emptyAddStage();
        String current=findCurrentBranch().toString();
        File currentBranch=Utils.join(HEADS,current);
        Utils.writeContents(currentBranch,newCommit.getID());
        Utils.writeContents(HEAD,newCommit.getID());
    }

    private static StringBuilder findCurrentBranch(){
        StringBuilder sb=new StringBuilder();
        File[] files=HEADS.listFiles();
        String head=Utils.readContentsAsString(HEAD);
        String heads=new String();
        for(File file:files){
            if(Utils.readContentsAsString(file).equals(head)){
                heads=file.getName();
                sb.append(file.getName());
                break;
            }
        }
        return sb;
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
                        if(key.equals(Utils.sha1(Utils.readContents(file)))){
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
        List<Commit> commitList=BFSCommit();
        Commit[] commits=HeapSort(commitList);
        gotLog(commits);
    }

    private static void gotLog(Commit[] commits){
        for(Commit commit:commits){
            StringBuilder sb=new StringBuilder();
            sb.append("==="+"\n");
            sb.append("commit "+commit.getID()+"\n");
            if(commit.getParentID().size()==1) {
                sb.append("Date: " + commit.getTimeStamp() + "\n");
                sb.append(commit.getMessage()+"\n");
            }else{
                StringBuilder first=new StringBuilder(commit.getParentID().get(0));
                StringBuilder second=new StringBuilder(commit.getParentID().get(1));
                sb.append("Merge: "+first.substring(0,7)+" "+second.substring(0,7));
                sb.append("Date: "+commit.getTimeStamp());
                sb.append(" "+"\n");
            }
            System.out.println(sb.toString());
        }
    }

    private static List<Commit> BFSCommit(){//对图做广度遍历,得到一个Commit的List序列
        List<Commit> commitList=new ArrayList<>();
        String head=Utils.readContentsAsString(HEAD);
        File[] files=OBJECTS.listFiles();
        Queue<Commit> que=new LinkedList<>();
        Set<Commit> visited=new HashSet<>();
        File[] filesBranch=HEADS.listFiles();
        for(File file:filesBranch){
            String branchCommitID=Utils.readContentsAsString(file);
            Commit branchCommit=findCommitByCommitID(branchCommitID);
            que.offer(branchCommit);
            visited.add(branchCommit);
        }
        while(!que.isEmpty()){
            Commit vet=que.poll();
            commitList.add(vet);
            List<String> parents=vet.getParentID();
            //遍历邻接顶点
            outer:
            for(String parent:parents){
                for(File file:files){
                    if(file.getName().equals(parent)){
                        Commit commit=Utils.readObject(file,Commit.class);
                        Iterator<Commit> iterator= visited.iterator();
                        while(iterator.hasNext()){
                            Commit commit1=iterator.next();
                            if(commit1.getID().equals(commit.getID())){
                                continue outer;
                            }
                        }
                        que.offer(commit);
                        visited.add(commit);
                    }
                }
            }
            //parents=vet.getParentID();
        }
        return commitList;
    }

    private static Commit[]  HeapSort(List<Commit> commitList){//堆排序
        Commit[] commitArray=commitList.toArray(new Commit[commitList.size()]);
        for(int i=(commitArray.length/2)-1;i>=0;i--){
            adjustHeap(commitArray,i,commitArray.length);
        }
        for(int i=commitArray.length-1;i>0;i--){
            Swap(commitArray,0,i);
            adjustHeap(commitArray,0,i);
        }
        return commitArray;
    }

    private static void adjustHeap(Commit[] arr,int i,int length){
        Commit temp=arr[i];
        for(int k=2*i+1;k<length;k=k*2+1){
            if(k+1<length&&arr[k].compareTo(arr[k+1])>0){
                k++;
            }
            if(arr[k].compareTo(temp)<0){
                arr[i]=arr[k];
                i=k;
            }else{
                break;
            }
        }
        arr[i]=temp;
    }

    private static void Swap(Commit[] arr,int i,int j){
        Commit temp=arr[i];
        arr[i]=arr[j];
        arr[j]=temp;
    }

    public static void global_log(){
        List<Commit> commitList=BFSCommit();
        Commit[] commits=HeapSort(commitList);
        for(Commit commit:commits){
            System.out.println(commit.getMessage());
        }
    }

    public static void find(String message){
        List<Commit> commitList=BFSCommit();
        Commit[] commits=HeapSort(commitList);
        for(Commit commit:commits){
            if(commit.getMessage().equals(message)){
                System.out.println(commit.getID()+"\n");
                return;
            }
        }
        System.out.println("Found no commit with that message.");
    }

    public static void status(){
        StringBuilder sb=new StringBuilder();
        sb.append("=== Branches ==="+"\n");
        sb.append(findBranches());
        sb.append("=== Staged Files ==="+"\n");
        sb.append(findAddStaged());
        sb.append("=== Removed Files ==="+"\n");
        sb.append(findRemoveStage());
        System.out.println(sb.toString());
    }

    private static StringBuilder findBranches(){
        StringBuilder sb=new StringBuilder();
        File[] files=HEADS.listFiles();
        String head=Utils.readContentsAsString(HEAD);
        String heads=new String();
        for(File file:files){
            if(Utils.readContentsAsString(file).equals(head)){
                heads=file.getName();
                sb.append("*"+file.getName()+"\n");
                break;
            }
        }
        for(File file:files){
            if(file.getName().equals(heads)){
                continue;
            }
            sb.append(file.getName()+"\n");
        }
        return sb;
    }

    private static StringBuilder findAddStaged(){
        StringBuilder sb=new StringBuilder();
        if(Utils.readContents(ADDSTAGE).length==0) return sb;
        Stage addStage=Utils.readObject(ADDSTAGE,Stage.class);
        for(String filePath:addStage.getPathToBlobID().keySet()){
            File file=new File(filePath);
            if(file.exists()) sb.append(file.getName()+"\n");
        }
        return sb;
    }

    private static StringBuilder findRemoveStage(){
        StringBuilder sb=new StringBuilder();
        if(Utils.readContents(REMOVESTAGE).length==0) return sb;
        Stage removeStage=Utils.readObject(REMOVESTAGE,Stage.class);
        for(String filePath:removeStage.getPathToBlobID().keySet()){
            File file=new File(filePath);
            sb.append(file.getName()+'\n');
        }
        return sb;
    }

    public static void checkout(String commitID,String fileName,String branchName) throws IOException {
        if(commitID==null&&fileName!=null&&branchName==null){
            File file=Utils.join(CWD,fileName);
            if(isDuplicatedCommit(file)){
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            checkOutFile(file);
        }else if(commitID!=null&&fileName!=null&&branchName==null){
            File file=Utils.join(CWD,fileName);
            if(!isCommitExists(commitID)){
                System.out.println("No commit with that id exists.");
            }
            Commit commit=findCommitByCommitID(commitID);
            if(isDuplicatedInSpecificCommit(file,commit)){
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            checkOutFileFromCommit(commit,file);
        }else if(commitID==null&&fileName==null&&branchName!=null){
            if(!isBranchExists(branchName)){
                System.out.println("No such branch exists.");
                System.exit(0);
            }
            if(branchName.equals(findCurrentBranch())){
                System.out.println("No need to checkout the current branch.");
                System.exit(0);
            }
            checkOutBranch(branchName);
        }
    }

    private static boolean isCommitExists(String commitID){
        File[] files=OBJECTS.listFiles();
        for(File file:files){
            if(file.getName().equals(commitID)){
                return true;
            }
        }
        return false;
    }

    private static boolean isDuplicatedInSpecificCommit(File file,Commit commit){
        for(String filePath:commit.getPathToBlobID().keySet()){
            if(file.getPath().equals(filePath)) return true;
        }
        return false;
    }

    private static void checkOutFile(File fileCheck) throws IOException {//获取头提交中存在的文件版本并将其放入工作目录中，覆盖已存在的文件版本（如果存在）
        String headID=Utils.readContentsAsString(HEAD);//取出当前commit的id
        File[] files=OBJECTS.listFiles();
        for(File file:files){
            if(file.getName().equals(headID)){//找到当前包含commit对象的文件
                Commit newCommit=Utils.readObject(file,Commit.class);
                for(String path:newCommit.getPathToBlobID().keySet()){//遍历commit对象的键值对的键--跟踪的文件的路径
                    Blob blob=findBlobByBlobID(newCommit.getPathToBlobID().get(path));//找到Blob文件
                    if(blob.getFilePath().equals(fileCheck.getPath())){
                        byte[] newContents=blob.getSaveFileBytes();
                        String str=new String(newContents, StandardCharsets.UTF_8);
                        Utils.writeContents(fileCheck,str);
                    }
                }
            }
        }
    }

    private static Blob findBlobByBlobID(String BlobID){
        File[] files=OBJECTS.listFiles();
        for(File file:files){
            if(file.getName().equals(BlobID)){
                return Utils.readObject(file,Blob.class);
            }
        }
        return null;
    }

    private static File findFileFromBlob(String BlobID){
        File[] files=OBJECTS.listFiles();
        for(File file:files){
            if(file.getName().equals(BlobID)){
                Blob blob=Utils.readObject(file,Blob.class);
                return blob.getFile();
            }
        }
        return null;
    }

    public static Commit findCommitByCommitID(String commitID){//私有方法
        File[] files=OBJECTS.listFiles();
        for(File file:files){
            if(file.getName().equals(commitID)){
                return Utils.readObject(file,Commit.class);
            }
        }
        return null;
    }

    private static void checkOutFileFromCommit(Commit commit,File fileCheckout) throws IOException {
        //获取具有给定 id 的提交中存在的文件版本，并将其放入工作目录中，覆盖已存在的文件版本（如果存在）
        if(commit==null) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        File[] files=OBJECTS.listFiles();
        for(String path:commit.getPathToBlobID().keySet()){
            if(fileCheckout.getPath().equals(path)){//找到BlobID
                for(File file:files){
                    if(file.getName().equals(commit.getPathToBlobID().get(path))){
                        Blob blob=Utils.readObject(file,Blob.class);
                        byte[] newContents= blob.getSaveFileBytes();
                        String str=new String(newContents,StandardCharsets.UTF_8);
                        Utils.writeContents(fileCheckout,str);
                    }
                }
            }
        }
    }

    private static void checkOutBranch(String branchName) throws IOException{
        /*获取给定分支头部提交中的所有文件，并将它们放入工作目录中，覆盖已存在的文件的版本（如果存在）。
        此外，在此命令结束时，给定分支现在将被视为当前分支 (HEAD)。当前分支中跟踪但不存在于签出分支中的任何文件都将被删除。
        暂存区域将被清除，除非签出的分支是当前分支
         */
        Commit newCommit=findCommitByBranchName(branchName);
        Commit oldCommit=findCommitByCommitID(Utils.readContentsAsString(HEAD));
        File[] CWDFiles=CWD.listFiles();
        List<File> newFileList=new ArrayList<>();
        for(String path:newCommit.getPathToBlobID().keySet()){
            Blob blob=findBlobByBlobID(newCommit.getPathToBlobID().get(path));
            newFileList.add(blob.getFile());
        }
        File[] newFile=newFileList.toArray(new File[newFileList.size()]);
        iterateSituations(CWDFiles,oldCommit,newCommit);
        iterateSituations(newFile,oldCommit,newCommit);
    }

    private static void iterateSituations(File[] files,Commit oldCommit,Commit newCommit) throws IOException {
        for(File file:files){
            if(isTrackedByNewCommit(file,newCommit)&&isTrackedByOldCommit(file)){
                TrackedBothOf(file,newCommit);
            }else if(!isTrackedByOldCommit(file)&&isTrackedByNewCommit(file,newCommit)){
                //newCommit追踪的文件,工作区的文件,如果同名,则报错并退出
                if(isDuplicatedInSpecificCommit(file,newCommit)&&isFileInCWD(file)){
                    System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }
                TrackedByNewNoOld(file,newCommit);
            }else if(isTrackedByOldCommit(file)&&!isTrackedByNewCommit(file,newCommit)){
                TrackedByOldNotNew(file);
            }
        }
        afterCheck(newCommit);
    }

    private static boolean isFileInCWD(File file){
        for(File file1:CWD.listFiles()){
            if(file.getName().equals(file1.getName())){
                return true;
            }
        }
        return false;
    }

    private static boolean isTrackedByNewCommit(File file,Commit newCommit){
        for(String path:newCommit.getPathToBlobID().keySet()){
            if(path.equals(file.getPath())){
                return true;
            }
        }
        return false;
    }


    private static boolean isTrackedByOldCommit(File file){
        Commit oldCommit=findCommitByCommitID(Utils.readContentsAsString(HEAD));
        for(String path:oldCommit.getPathToBlobID().keySet()){
            if(path.equals(file.getPath())){
                return true;
            }
        }
        return false;
    }

    private static void TrackedBothOf(File file,Commit newCommit){//文件被两者跟踪
        //byte[] newContents=Utils.readContents(file);
        for(String path:newCommit.getPathToBlobID().keySet()) {
            if (path.equals(file.getPath())) {
                Blob blob = findBlobByBlobID(newCommit.getPathToBlobID().get(path));
                byte[] newContents = blob.getSaveFileBytes();
                String str=new String(newContents,StandardCharsets.UTF_8);
                Utils.writeContents(file,str);
            }
        }
    }

    private static void TrackedByOldNotNew(File file){//被旧分支追踪但不被新分支追踪
        Utils.restrictedDelete(file);
    }

    private static void TrackedByNewNoOld(File file,Commit newCommit) throws IOException {
        for(String path:newCommit.getPathToBlobID().keySet()){
            if(path.equals(file.getPath())){
                Blob blob=findBlobByBlobID(newCommit.getPathToBlobID().get(path));
                byte[] newContents= blob.getSaveFileBytes();
                String str=new String(newContents,StandardCharsets.UTF_8);
                Utils.writeContents(file,str);
            }
        }
    }

    //---------------------------------------------------------------------------------------------------

    private static Commit findCommitByBranchName(String branchName){//这个branch的最新commit
        File[] files=HEADS.listFiles();
        for(File file:files){
            if(branchName.equals(file.getName())) {
                String commitID = Utils.readContentsAsString(file);
                return findCommitByCommitID(commitID);
            }
        }
        System.out.println("No such branch exists.");
        System.exit(0);
        return null;
    }

    private static void afterCheck(Commit newCommit){
        Commit oldCommit=findCommitByCommitID(Utils.readContentsAsString(HEAD));
        Utils.writeContents(HEAD,newCommit.getID());
    }

    public static void branch(String branchName) throws IOException {
        for(File file:HEADS.listFiles()){
            if(file.getName().equals(branchName)){
                System.out.println("A branch with that name already exists.");
                System.exit(0);
            }
        }
        File file=Utils.join(HEADS,branchName);
        file.createNewFile();
        String Head=Utils.readContentsAsString(HEAD);
        Utils.writeContents(file,Head);
    }

    public static void rm_branch(String branchName){
        if(branchName.equals(findCurrentBranch())){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        if(!isBranchExists(branchName)){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        File branchFile=findBranchFile(branchName);
        branchFile.delete();
    }

    private static boolean isBranchExists(String branchName){
        File[] files=HEADS.listFiles();
        for(File file:files){
            if(file.getName().equals(branchName)){
                return true;
            }
        }
        return false;
    }

    private static File findBranchFile(String branchName){
        File[] files=HEADS.listFiles();
        for(File file:files){
            if(file.getName().equals(branchName)){
                return file;
            }
        }
        return null;
    }

    public static void reset(String commitID) throws IOException {//改变HEAD为指定commit,并将暂存区和工作目录和指定commit的内容保持不变
        if(isCommitExists(commitID)){
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit newCommit=findCommitByCommitID(commitID);
        Commit oldCommit=findCommitByCommitID(Utils.readContentsAsString(HEAD));
        File[] CWDFiles=CWD.listFiles();
        List<File> newFileList=new ArrayList<>();
        for(String path:newCommit.getPathToBlobID().keySet()){
            Blob blob=findBlobByBlobID(newCommit.getPathToBlobID().get(path));
            newFileList.add(blob.getFile());
        }
        File[] newFile=newFileList.toArray(new File[newFileList.size()]);
        iterateSituations(CWDFiles,oldCommit,newCommit);
        iterateSituations(newFile,oldCommit,newCommit);
        afterCheck(newCommit);
    }

    public static void merge(String branchName) throws IOException {//找到spilt point
        Commit givenCommit=findCommitByBranchName(branchName);
        Commit currentCommit=findCommitByCommitID(Utils.readContentsAsString(HEAD));
        Commit splitPoint=findSplitPoint(currentCommit,givenCommit);
        verifyBeforeMerge(currentCommit,branchName);
        List<File> fileList=findAllFiles(splitPoint,givenCommit,currentCommit);

        Commit newCommit=new Commit("merge commit");
        List<String> parentID=new ArrayList<>();
        parentID.add(currentCommit.getID());
        parentID.add(givenCommit.getID());
        newCommit.setParentID(parentID);
        newCommit.setTimeStamp(parentID.get(0));
        List<String> fileNames=new ArrayList<>();
        Map<String,String> pathToBlobID=new HashMap<>();
        for(File file:fileList){
            if(isOnSplitPoint(splitPoint,file)){//存在于split point
                if((isOnCurrent(currentCommit,file)&&isOnGiven(givenCommit,file))){//同时存在于given/current
                    //确保不会引起冲突的条件判断;
                    if((isModifiedByCurrent(splitPoint,currentCommit,file)&&!isModifiedByGiven(splitPoint,givenCommit,file))
                            ||(isTheSameOnCurrentAndGiven(currentCommit,givenCommit,file))){//在current中改动或两者改动相同,工作区不做改动
                        fileNames.add(file.getName());
                        pathToBlobID.put(file.getPath(),currentCommit.getPathToBlobID().get(file.getPath()));
                        continue;
                    }
                    if(!isOnCurrent(currentCommit,file)&&!isOnGiven(givenCommit,file)) {
                        continue;
                    }//同时不存在于两者之间,工作区不做改动
                    if((!isModifiedByCurrent(splitPoint,currentCommit,file)&&
                            isModifiedByGiven(splitPoint,givenCommit,file))){//given有修改但current没有
                        if(isInCWDAndNotTrackedByCurrent(file,currentCommit)){
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                            System.exit(0);
                        }
                        //对工作区文件做改动,替换成givenCommit的文件
                        byte[] newContents=findFileBytesFromCommit(givenCommit,file);
                        String str=new String(newContents,StandardCharsets.UTF_8);
                        Utils.writeContents(file,str);
                        fileNames.add(file.getName());
                        pathToBlobID.put(file.getPath(),givenCommit.getPathToBlobID().get(file.getPath()));
                    }
                    //其中包含一个冲突判断,需处理
                    if(isModifiedByGiven(splitPoint,givenCommit,file)&&isModifiedByCurrent(splitPoint,currentCommit,file)){
                        //对当前的文件作一个合的并
                        byte[] currentBytes=findFileBytesFromCommit(currentCommit,file);
                        String strCurrent=new String(currentBytes);
                        byte[] givenBytes=findFileBytesFromCommit(givenCommit,file);
                        String strGiven=new String(givenBytes);
                        StringBuilder sb=new StringBuilder();
                        sb.append("<<<<<<< ");
                        sb.append("HEAD"+"\n");
                        sb.append(strCurrent+"\n");
                        sb.append("======="+"\n");
                        sb.append(strGiven+"\n");
                        sb.append(">>>>>>>");
                        Utils.writeContents(file,sb.toString());

                        fileNames.add(file.getName());
                        Blob blob=new Blob();
                        blob.addFile(file.getPath(),file);
                        String id=blob.setID();
                        blob.saveFile();
                        pathToBlobID.put(file.getPath(),id);
                    }
                }else if(!isOnGiven(givenCommit,file)&&isOnCurrent(currentCommit,file)){//在当前分支中存在,给定中不存在
                    if(!isModifiedByCurrent(splitPoint,currentCommit,file)){
                        if(isInCWDAndNotTrackedByCurrent(file,currentCommit)){
                            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                            System.exit(0);
                        }
                        //删除工作区的文件(无需加到新commit中)
                        Utils.restrictedDelete(file);
                    }
                }else if(isOnGiven(givenCommit,file)&&!isOnCurrent(currentCommit,file)){//在给定分支中,不在当前分支中
                    if(!isModifiedByGiven(splitPoint,givenCommit,file)){//在given中无修改
                        continue;//保持文件的不存在
                    }
                }
            }else if(!isOnSplitPoint(splitPoint,file)){//不在分支点上
                if(isOnCurrent(currentCommit,file)&&!isOnGiven(givenCommit,file)){//在current中,不在given中,不对工作区文件作操作
                    fileNames.add(file.getName());
                    pathToBlobID.put(file.getPath(),currentCommit.getPathToBlobID().get(file.getPath()));
                    continue;
                }else if(!isOnCurrent(currentCommit,file)&&isOnGiven(givenCommit,file)){//在given中,不在current中,
                    //checked out and staged 没懂
                    add(file);
                }
            }
        }
        newCommit.setFileName(fileNames);
        newCommit.setPathToBlobID(pathToBlobID);
        newCommit.setID();
        newCommit.saveCommit();
        String newBranch=findCurrentBranch().toString();
        for(File file:HEADS.listFiles()){
            if(file.getName().equals(newBranch)){
                Utils.writeContents(file,newCommit.getID());
            }
        }
        Utils.writeContents(HEAD,newCommit.getID());
    }

    private static void verifyBeforeMerge(Commit currentCommit,String branchName){
        boolean check=true;
        for(File file:HEADS.listFiles()){
            if(file.getName().equals(branchName)){
                check=true;
            }
        }
        if(check==false){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit givenCommit=findCommitByBranchName(branchName);
        if(Utils.readContents(ADDSTAGE).length!=0||Utils.readContents(REMOVESTAGE).length!=0){
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if(currentCommit.getID().equals(givenCommit.getID())){
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }

    private static boolean isInCWDAndNotTrackedByCurrent(File file,Commit currentComit){//是否在工作区而且被当前提交跟踪
        for(File fileCWD:CWD.listFiles()){
            if(file.getName().equals(fileCWD.getName())){
                for(String path:currentComit.getPathToBlobID().keySet()){
                    Blob blob=findBlobByBlobID(currentComit.getPathToBlobID().get(path));
                    if(file.getPath().equals(blob.getFilePath())){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static List<File> findAllFiles(Commit splitPoint,Commit givenCommit,Commit currentCommit){
        List<File> fileList=new ArrayList<>();
        Set<String> givenPaths=givenCommit.getPathToBlobID().keySet();
        Set<String> currentPaths=currentCommit.getPathToBlobID().keySet();
        Set<String> splitPaths=splitPoint.getPathToBlobID().keySet();
        for(String givenPath:givenPaths){
            String blobID=givenCommit.getPathToBlobID().get(givenPath);
            Blob blob=findBlobByBlobID(blobID);
            fileList.add(blob.getFile());
        }
        for(String currenPath:currentPaths){
            String blobID=currentCommit.getPathToBlobID().get(currenPath);
            Blob blob=findBlobByBlobID(blobID);
            fileList.add(blob.getFile());
        }
        for(String splitPath:splitPaths){
            String blobID=splitPoint.getPathToBlobID().get(splitPath);
            Blob blob=findBlobByBlobID(blobID);
            fileList.add(blob.getFile());
        }
        List<File> newFileList=new ArrayList<>();
        outer:
        for(File file:fileList){
            for(File file1:newFileList){
                if(file.getPath().equals(file1.getPath())){
                    continue outer;
                }
            }
            newFileList.add(file);
        }
        return newFileList;
    }

    public static Commit findSplitPoint(Commit currentCommit,Commit givenCommit){
        Commit currentCommitAncestor=findCommitByCommitID(currentCommit.getID());
        Commit givenCommitAncestor=findCommitByCommitID(givenCommit.getID());
        File[] files=OBJECTS.listFiles();
        while(!currentCommitAncestor.getID().equals(givenCommitAncestor.getID())){
            for(File file:files){
                String currentParentID= currentCommitAncestor.getParentID().get(0);
                if(currentParentID.equals(file.getName())){
                    currentCommitAncestor=findCommitByCommitID(file.getName());
                }
                String givenParentID=givenCommitAncestor.getParentID().get(0);
                if(givenParentID.equals(file.getName())){
                    givenCommitAncestor=findCommitByCommitID(file.getName());
                }
            }
        }
        return currentCommitAncestor;
    }

    private static byte[] findFileBytesFromCommit(Commit commit,File file){
        Set<String> commitPaths=commit.getPathToBlobID().keySet();
        for(String commitPath:commitPaths){
            if(file.getPath().equals(commitPath)){
                Blob blob=findBlobByBlobID(commit.getPathToBlobID().get(commitPath));
                return blob.getSaveFileBytes();
            }
        }
        return null;
    }

    private static boolean isTheSameOnCurrentAndGiven(Commit currentCommit,Commit givenCommit,File file){
        Set<String> currentPaths=currentCommit.getPathToBlobID().keySet();
        Set<String> givenPaths=givenCommit.getPathToBlobID().keySet();
        for(String currentPath:currentPaths){
            String currentBiobID=currentCommit.getPathToBlobID().get(currentPath);
            Blob currentBlob=findBlobByBlobID(currentBiobID);
            for(String givenPath:givenPaths){
                String givenBlobID=givenCommit.getPathToBlobID().get(givenPath);
                Blob givenBlob=findBlobByBlobID(givenBlobID);
                if(currentBlob.getFilePath().equals(givenBlob.getFilePath())){
                    if(file.getPath().equals(currentBlob.getFilePath())){
                        if(currentBlob.getSaveFileBytes().equals(givenBlob.getSaveFileBytes())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isOnSplitPoint(Commit splitPoint,File file){//文件是否存在于分割点
        Set<String> paths=splitPoint.getPathToBlobID().keySet();
        return judegeOnCommit(paths,file,splitPoint);
    }

    private static boolean isOnCurrent(Commit currentCommit,File file){//文件是否存在于当前分支
        Set<String> paths=currentCommit.getPathToBlobID().keySet();
        return judegeOnCommit(paths,file,currentCommit);
    }

    private static boolean isOnGiven(Commit givenCommit,File file){////文件是否存在于给定分支
        Set<String> paths=givenCommit.getPathToBlobID().keySet();
        return judegeOnCommit(paths,file,givenCommit);
    }

    private static boolean judegeOnCommit(Set<String> paths,File file,Commit commit){
        for(String path:paths){
            String blobID=commit.getPathToBlobID().get(path);
            Blob blob=findBlobByBlobID(blobID);
            if(file.getPath().equals(blob.getFilePath())){
                return true;
            }
        }
        return false;
    }

    public static boolean isModifiedByCurrent(Commit splitPoint,Commit currentComit,File file){//私有方法
        Set<String> splitPaths=splitPoint.getPathToBlobID().keySet();
        Set<String> currentPaths=currentComit.getPathToBlobID().keySet();
        return judgeByModified(splitPaths,currentPaths,splitPoint,currentComit,file);
    }

    public static boolean isModifiedByGiven(Commit splitPoint,Commit givenCommit,File file){//私有方法
        Set<String> splitPaths=splitPoint.getPathToBlobID().keySet();
        Set<String> givenPaths=givenCommit.getPathToBlobID().keySet();
        return judgeByModified(splitPaths,givenPaths,splitPoint,givenCommit,file);
    }

    private static boolean judgeByModified(Set<String> splitPaths,Set<String> commitPaths,Commit splitPoint,Commit commit,File file){
        for(String splitPath:splitPaths){
            String splitBlobID=splitPoint.getPathToBlobID().get(splitPath);
            Blob splitBlob=findBlobByBlobID(splitBlobID);
            for(String commitPath:commitPaths){
                String BlobID=commit.getPathToBlobID().get(commitPath);
                Blob Blob=findBlobByBlobID(BlobID);
                if(Blob.getFilePath().equals(splitBlob.getFilePath())){
                    if(file.getPath().equals(Blob.getFilePath())){//找到current和splitPoint中的Blob,且对应于file
                        System.out.println("bijiao"+Arrays.equals(Blob.getSaveFileBytes(),splitBlob.getSaveFileBytes()));
                        if(!Arrays.equals(Blob.getSaveFileBytes(),splitBlob.getSaveFileBytes())){
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
