package simpledb.materialize;

import java.util.Collection;
import java.util.List;

import simpledb.query.Constant;
import simpledb.query.TablePlan;
import simpledb.query.UpdateScan;
import simpledb.record.Schema;
import simpledb.record.TableInfo;
import simpledb.server.SimpleDB;
import simpledb.tx.Transaction;

/* CS4432-Project2:
 * This class extends the original SortScan class and overwrites all of it functions
 * so that if the table is already sorted, it will use it directly instead of sorting them again
 * */
public class SortScanExtend extends SortScan {
    private TableInfo tableInfo;
    private Schema schema;
    private Transaction tx;
    private UpdateScan originalTable;
    private Collection<String> tableFields;
    private boolean isSorted;

    public SortScanExtend(List<TempTable> runs, RecordComparator comp,
                          TablePlan p, Transaction tx) {
        super(runs, comp);
        this.originalTable = (UpdateScan) p.open();
        this.tableInfo = p.gettblinfo();
        this.schema = this.tableInfo.schema();
        this.tableFields = this.schema.fields();
        this.isSorted = this.tableInfo.srtflgCheck();
        this.tx = tx;
        //this.originalTable.beforeFirst();
    }

    public void beforeFirst() {
        if (this.isSorted) {
            this.originalTable.beforeFirst();
        } else {
            super.beforeFirst();
        }
    }

    public boolean next() {
        boolean originalNext = this.originalTable.next();

        if (this.tableInfo.srtflgCheck()) {
            return originalNext;
        } else {
            boolean next = super.next();
            // copy sorted table into original table

            if (next) {
                if(!originalNext) {
                    this.originalTable.insert();
                }
                for (String fieldName : this.tableFields) {
                    this.originalTable.setVal(fieldName,
                            super.getVal(fieldName));
                }
            } else {
                System.out.println("next" + next);
                System.out.println("original next" + originalNext);
                this.tableInfo.setsrtflg(true);
                SimpleDB.mdMgr().updateSortedTable(this.tableInfo, tx);
            }

            return next;
        }
    }

    public void close() {
        super.close();
        this.originalTable.close();
    }

    public Constant getVal(String fieldName) {
        if (this.isSorted) {
            return this.originalTable.getVal(fieldName);
        } else {
            return super.getVal(fieldName);
        }
    }

    public int getInt(String fieldName) {
        if (this.isSorted) {
            return this.originalTable.getInt(fieldName);
        } else {
            return super.getInt(fieldName);
        }
    }

    public String getString(String fieldName) {
        if (this.isSorted) {
            return this.originalTable.getString(fieldName);
        } else {
            return super.getString(fieldName);
        }
    }

    public boolean hasField(String fieldName) {
        if (this.isSorted) {
            return this.originalTable.hasField(fieldName);
        } else {
            return super.hasField(fieldName);
        }
    }

    public void savePosition() {
        if(!this.isSorted) {
            super.savePosition();
        }
    }

    public void restorePosition() {
        if(!this.isSorted) {
            super.restorePosition();
        }
    }
}