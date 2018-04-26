package simpledb.index.exthash;

import simpledb.index.Index;
import simpledb.index.hash.HashIndex;
import simpledb.query.Constant;
import simpledb.query.TableScan;
import simpledb.record.RID;
import simpledb.record.Schema;
import simpledb.record.TableInfo;
import simpledb.tx.Transaction;

import java.util.ArrayList;

public class ExtensibleHashIndex implements Index {
    private String idxname;
    private Schema sch;
    private Transaction tx;
    private Constant searchkey = null;
    private TableScan ts = null;
    private ArrayList<Bucket> buckets = new ArrayList<Bucket>();
    private int globalDepth = 2;
    private int firstTime = 0;

    /**
     * Opens a hash index for the specified index.
     * @param idxname the name of the index
     * @param sch the schema of the index records
     * @param tx the calling transaction
     */
    public ExtensibleHashIndex(String idxname, Schema sch, Transaction tx) {
        this.idxname = idxname;
        this.sch = sch;
        this.tx = tx;
    }

    /**
     * Positions the index before the first index record
     * having the specified search key.
     * The method hashes the search key to determine the bucket,
     * and then opens a table scan on the file
     * corresponding to the bucket.
     * The table scan for the previous bucket (if any) is closed.
     * @see simpledb.index.Index#beforeFirst(simpledb.query.Constant)
     */
    public void beforeFirst(Constant searchkey) {
        if(firstTime == 0){
            Bucket b1 = new Bucket("00",2);
            Bucket b2 = new Bucket("01",2);
            Bucket b3 = new Bucket("10",2);
            Bucket b4 = new Bucket("11",2);
            buckets.add(b1);
            buckets.add(b2);
            buckets.add(b3);
            buckets.add(b4);
            firstTime = 1;
        }
        close();
        this.searchkey = searchkey;

        //CS4432-Project2:
        String value = this.constantToBinary(searchkey, globalDepth);
        String tblname = idxname + value;
        TableInfo ti = new TableInfo(tblname, sch);
        ts = new TableScan(ti, tx);
    }

    /**
     * Moves to the next record having the search key.
     * The method loops through the table scan for the bucket,
     * looking for a matching record, and returning false
     * if there are no more such records.
     * @see simpledb.index.Index#next()
     */
    public boolean next() {
        while (ts.next())
            if (ts.getVal("dataval").equals(searchkey))
                return true;
        return false;
    }

    /**
     * Retrieves the dataRID from the current record
     * in the table scan for the bucket.
     * @see simpledb.index.Index#getDataRid()
     */
    public RID getDataRid() {
        int blknum = ts.getInt("block");
        int id = ts.getInt("id");
        return new RID(blknum, id);
    }

    /**
     * Inserts a new record into the table scan for the bucket.
     * @see simpledb.index.Index#insert(simpledb.query.Constant, simpledb.record.RID)
     */
    public void insert(Constant val, RID rid) {
        beforeFirst(val);
        for (Bucket bucket: buckets){
            if(bucket.getBucket().equals(val)){
                if(bucket.getValues().size() !=4){
                    bucket.addValue(searchkey);
                    String tblname = idxname + val;
                    TableInfo ti = new TableInfo(tblname, sch);
                    ts = new TableScan(ti, tx);
                }
                else{
                    int localDepth = bucket.getLocalDepth();
                    if(localDepth<globalDepth){
                        localDepth++;
                        Bucket newBucket0 = new Bucket("0"+bucket.getBucket(),localDepth);
                        Bucket newBucket1 = new Bucket("1"+bucket.getBucket(),localDepth);
                        ArrayList<Constant> oldValues = bucket.getValues();
                        for (Constant value: oldValues){
                            String temp = constantToBinary(value,localDepth);
                            if (temp == newBucket0.getBucket()){
                                newBucket0.addValue(value);
                            }else{
                                newBucket1.addValue(value);
                            }
                        }
                        buckets.remove(bucket);
                        String temp1 = constantToBinary(val,localDepth);
                        if(temp1.equals(newBucket0.getBucket())){
                            newBucket0.addValue(val);
                        }
                        else {
                            newBucket1.addValue(val);
                        }
                        buckets.add(newBucket0);
                        buckets.add(newBucket1);
                        String tblname = idxname + val;
                        TableInfo ti = new TableInfo(tblname, sch);
                        ts = new TableScan(ti, tx);
                    }
                    else{
                        globalDepth++;
                        localDepth++;
                        Bucket newBucket0 = new Bucket("0"+bucket.getBucket(),localDepth);
                        Bucket newBucket1 = new Bucket("1"+bucket.getBucket(),localDepth);
                        ArrayList<Constant> oldValues = bucket.getValues();
                        for (Constant value: oldValues){
                            String temp = constantToBinary(value,localDepth);
                            if (temp == newBucket0.getBucket()){
                                newBucket0.addValue(value);
                            }else{
                                newBucket1.addValue(value);
                            }
                        }
                        buckets.remove(bucket);
                        String temp1 = constantToBinary(val,localDepth);
                        if(temp1.equals(newBucket0.getBucket())){
                            newBucket0.addValue(val);
                        }
                        else {
                            newBucket1.addValue(val);
                        }
                        buckets.add(newBucket0);
                        buckets.add(newBucket1);
                        String tblname = idxname + val;
                        TableInfo ti = new TableInfo(tblname, sch);
                        ts = new TableScan(ti, tx);
                    }

                }
            }
        }


        ts.insert();
        ts.setInt("block", rid.blockNumber());
        ts.setInt("id", rid.id());
        ts.setVal("dataval", val);
        System.out.println(toString());
    }

    /**
     * Deletes the specified record from the table scan for
     * the bucket.  The method starts at the beginning of the
     * scan, and loops through the records until the
     * specified record is found.
     * @see simpledb.index.Index#delete(simpledb.query.Constant, simpledb.record.RID)
     */
    public void delete(Constant val, RID rid) {
        beforeFirst(val);
        while(next())
            if (getDataRid().equals(rid)) {
                ts.delete();
                return;
            }
    }

    /**
     * Closes the index by closing the current table scan.
     * @see simpledb.index.Index#close()
     */
    public void close() {
        if (ts != null)
            ts.close();
    }
/*
    *//**
     * Returns the cost of searching an index file having the
     * specified number of blocks.
     * The method assumes that all buckets are about the
     * same size, and so the cost is simply the size of
     * the bucket.
     * @param numblocks the number of blocks of index records
     * @param rpb the number of records per block (not used here)
     * @return the cost of traversing the index
     *//*
    public static int searchCost(int numblocks, int rpb){
        return numblocks / HashIndex.NUM_BUCKETS;
    }//todo*/


    /**
     * convert constant to a binary value
     * @param value constant need to convert
     * @param length the length of digits of the binary value
     * @return the last x digits of the binary value
     */
    //CS4432-Project2:
    public String constantToBinary(Constant value, int length){
        String test = Integer.toBinaryString(value.hashCode());
        int testLength = test.length();
        if(testLength >= length){
            test = test.substring(test.length()-length);
            return test;
        }
        else{
            int diff = length - testLength;
            for (int i = 0; i < diff ; i++){
                test = "0" + test;
            }
            return test;
        }
    }

    @Override
    public String toString() {
        return "ExtHashIndex{" +
                "idxname='" + idxname + '\'' +
                ", sch=" + sch +
                ", tx=" + tx +
                ", searchkey=" + searchkey +
                ", ts=" + ts +
                ", buckets=" + buckets +
                ", globalDepth=" + globalDepth +
                ", firstTime=" + firstTime +
                '}';
    }
}
