package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Stage implements Serializable {
    private Map<String,String> pathToBlobID= new TreeMap<>();

    public void addFile(String filePath, String bolbID){
        System.out.println(Utils.readContents(Repository.ADDSTAGE).length!=0);
        if(Utils.readContents(Repository.ADDSTAGE).length!=0){
            Map<String,String> old=Utils.readObject(Repository.ADDSTAGE,Stage.class).pathToBlobID;
            this.pathToBlobID=old;
            if(old.containsKey(filePath)){
                this.pathToBlobID.remove(filePath);
            }
        }
        this.pathToBlobID.put(filePath,bolbID);
    }

    public void addRemoveFile(String filePath,String blobID){
        if(Utils.readContents(Repository.REMOVESTAGE).length!=0){
            Map<String,String> old=Utils.readObject(Repository.REMOVESTAGE,Stage.class).pathToBlobID;
            this.pathToBlobID=old;
            if(old.containsKey(filePath)){
                this.pathToBlobID.remove(filePath);
            }
        }
        this.pathToBlobID.put(filePath,blobID);
    }

    public Map<String,String> getPathToBlobID(){
        return this.pathToBlobID;
    }

    public boolean isDuplicated(File file){
        Set<String> keys=this.pathToBlobID.keySet();
        for(String key : keys){
            System.out.println(Utils.sha1(Utils.serialize(file)));
            if(this.pathToBlobID.get(key).equals(Utils.sha1(Utils.serialize(file)))){
                return true;
            }
        }
        return false;
    }

    public void saveAddFile(){
        Utils.writeObject(Repository.ADDSTAGE,this);
    }
}
