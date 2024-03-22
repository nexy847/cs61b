package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable,Comparable<Commit> {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;//meta

    private String timeStamp;//meta

    private List<String> fileName;

    private Map<String,String> pathToBlobID;

    private List<String> parentID;

    private String ID;

    public Commit(String message){
        this.message=message;
    }

    /* TODO: fill in the rest of this class. */

    public void setTimeStamp(String parentID){
        if(parentID==null){
            Date date=new Date(0);
            this.timeStamp=dateToTimeStamp(date);
        }else{
            Date date=new Date();
            this.timeStamp=dateToTimeStamp(date);
        }
    }

    public void setParentID(List<String> parentID){
        this.parentID=parentID;
    }

    public void setPathToBlobID(Map<String,String> pathToBlobID){
        this.pathToBlobID=pathToBlobID;
    }

    public void setFileName(List<String> fileName){
        this.fileName=fileName;
    }

    private String dateToTimeStamp(Date date){
        DateFormat dateFormat=new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        return dateFormat.format(date);
    }

    public Map<String,String> getPathToBlobID(){
        return this.pathToBlobID;
    }

    public void setID(){
        this.ID=Utils.sha1(Utils.serialize(this));
        int t=3;
    }

    public String getID(){
        return this.ID;
    }

    public void saveCommit() throws IOException {
        File commit=Utils.join(Repository.OBJECTS,this.ID);
        commit.createNewFile();
        Utils.writeObject(commit,this);
    }

    public List<String> getParentID(){
        return this.parentID;
    }

    public String getTimeStamp(){
        return this.timeStamp;
    }

    public String getMessage(){
        return this.message;
    }

    @Override
    public int compareTo(Commit o) {
        DateFormat dateFormat=new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z", Locale.US);
        try {
            Date thisDate=dateFormat.parse(this.timeStamp);
            Date otherDate=dateFormat.parse(o.timeStamp);
            //越接近当下越小
            return thisDate.compareTo(otherDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
