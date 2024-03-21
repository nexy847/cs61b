package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class Blob implements Serializable {
    private String ID;

    private String filePath;//blob的文件地址

    private File saveFile;

    public void addFile(String filePath,File saveFile){
        this.filePath=filePath;
        this.saveFile=saveFile;
    }
    public String setID(){
        this.ID=Utils.sha1(Utils.serialize(saveFile));
        return this.ID;
    }

    public void saveFile() throws IOException {
        File blob=Utils.join(Repository.OBJECTS,this.ID);
        blob.createNewFile();
        Utils.writeObject(blob,this);
    }

}
