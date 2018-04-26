import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import simpledb.remote.SimpleDriver;

public class SMSJRunQuery {

    public static void main(String[] args) {
        Connection conn = null;
        Driver d = new SimpleDriver();
        // you may change host if your SimpleDB server is running on a different
        // machine
        String host = "localhost";
        String url = "jdbc:simpledb://" + host;
        Statement s = null;
        long time0, time1, time2, time3;
        String query;
        ResultSet rs;
        try {
            conn = d.connect(url, null);
            s = conn.createStatement();
            System.out.println(s);

            // Run a join query
            query = "select a1, a2, a3 from t1, t2 where a1 = a2";
            System.out.println(query + "\n");

            time0 = System.currentTimeMillis();
            System.out.println("1");

            rs = s.executeQuery(query);

            while (true) {
                boolean loop = false;
                try{
                    loop = rs.next();
                }catch(SQLException e){
                    System.out.println("NullPointerException in while loop");
                }
                if(loop) {
                    //System.out.println("3");
                    System.out.println("a1 : " + rs.getInt("a1") + " a3 : "
                            + rs.getInt("a3") + " a2 : " + rs.getInt("a2"));
                }
                else{
                    break;
                }
            }

            time1 = System.currentTimeMillis();
            System.out.println("--------------Query Running Time: " + (time1 - time0) + " ms\n");
            System.out.println("-----------------------------------------------------");

            // Run the same query again, this time it should be sorted and take
            // less time.
            query = "select a1, a2, a3 from t1, t2 where a1 = a2";
            System.out.println(query + "\n");
            time2 = System.currentTimeMillis();
            rs = s.executeQuery(query);


            while (true) {
                boolean loop = false;
                try{
                    loop = rs.next();
                }catch(SQLException e){
                    System.out.println("NullPointerException in while loop");
                }
                if(loop) {
                    System.out.println("a1 : " + rs.getInt("a1") + " a3 : "
                            + rs.getInt("a3") + " a2 : " + rs.getInt("a2"));
                }
                else{
                    break;
                }
            }

            time3 = System.currentTimeMillis();
            System.out.println("--------------Query Running Time: " + (time3 - time2) + " ms\n");
            System.out.println("-----------------------------------------------------");
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}