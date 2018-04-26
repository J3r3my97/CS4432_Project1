package simpledb.index.exthash;

import simpledb.query.Constant;
import java.util.ArrayList;

//CS4432-Project2:
public class Bucket {
    private String bucket;
    private int localDepth;
    private ArrayList<Constant> values = new ArrayList<Constant>();

    public Bucket(String bucket, int localDepth) {
        this.bucket = bucket;
        this.localDepth = localDepth;
    }

    public String getBucket() {
        return bucket;
    }

    public int getLocalDepth() {
        return localDepth;
    }

    public ArrayList<Constant> getValues() {
        return values;
    }

    public void addValue(Constant value){
        values.add(value);
    }
}
