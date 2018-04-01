import java.sql.*;
import simpledb.remote.SimpleDriver;

public class CreateFinalreport {
    public static void main(String[] args) {
        Connection conn = null;
        try {
            Driver d = new SimpleDriver();
            conn = d.connect("jdbc:simpledb://localhost", null);
            Statement stmt = conn.createStatement();

            String s = "create table Grade(SId int, SName varchar(10), SGpa int)";
            stmt.executeUpdate(s);
            System.out.println("Table Grade created.");

            s = "insert into Grade(SId, SName,SGpa) values ";
            String[] gradevals = {"(1, 'joe', 3)",
                    "(2, 'amy', 3)",
                    "(3, 'max', 4)",
                    "(4, 'sue', 2)",
                    "(5, 'bob', 3)",
                    "(6, 'kim', 3)",
                    "(7, 'art', 3)",
                    "(8, 'pat', 3)",
                    "(9, 'lee', 2)"};
            for (int i=0; i<gradevals.length; i++)
                stmt.executeUpdate(s + gradevals[i]);
            System.out.println("Grade records inserted.");
    }
        catch(SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (conn != null)
                    conn.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

}
