package mod.sin.wyvern;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.players.Player;
import com.wurmonline.server.utils.DbUtilities;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseHelper {
    public static final Logger logger = Logger.getLogger(DatabaseHelper.class.getName());

    public static void onPlayerLogin(Player p){
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        ResultSet rs = null;
        boolean foundLeaderboardOpt = false;
        try {
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt");
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("name").equals(p.getName())) continue;
                foundLeaderboardOpt = true;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        if (!foundLeaderboardOpt) {
            logger.info("No leaderboard entry for "+p.getName()+". Creating one.");
            dbcon = ModSupportDb.getModSupportDb();
            try {
                ps = dbcon.prepareStatement("INSERT INTO LeaderboardOpt (name) VALUES(?)");
                ps.setString(1, p.getName());
                ps.executeUpdate();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
            finally{
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
        boolean foundPlayerStats = false;
        dbcon = ModSupportDb.getModSupportDb();
        try {
            ps = dbcon.prepareStatement("SELECT * FROM PlayerStats");
            rs = ps.executeQuery();
            while (rs.next()) {
                if (!rs.getString("NAME").equals(p.getName())) continue;
                foundPlayerStats = true;
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        if (!foundPlayerStats) {
            logger.info("No player stats entry for "+p.getName()+". Creating one.");
            dbcon = ModSupportDb.getModSupportDb();
            try {
                ps = dbcon.prepareStatement("INSERT INTO PlayerStats (NAME) VALUES(\"" + p.getName() + "\")");
                ps.executeUpdate();
            }
            catch (SQLException e) {
                throw new RuntimeException(e);
            }
            finally{
                DbUtilities.closeDatabaseObjects(ps, null);
                DbConnector.returnConnection(dbcon);
            }
        }
    }

    public static void onServerStarted(){
        Connection con = ModSupportDb.getModSupportDb();
        try {
            String sql;
            String tableName = "LeaderboardOpt";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (name VARCHAR(30) NOT NULL DEFAULT 'Unknown', OPTIN INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try {
                    ps = con.prepareStatement(sql);
                    ps.execute();
                }
                finally{
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }
            tableName = "SteamIdMap";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', STEAMID LONG NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try{
                    ps = con.prepareStatement(sql);
                    ps.execute();
                }
                finally{
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }
            tableName = "PlayerStats";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', KILLS INT NOT NULL DEFAULT 0, DEATHS INT NOT NULL DEFAULT 0, DEPOTS INT NOT NULL DEFAULT 0, HOTAS INT NOT NULL DEFAULT 0, TITANS INT NOT NULL DEFAULT 0, UNIQUES INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try {
                    ps = con.prepareStatement(sql);
                    ps.execute();
                }
                finally{
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }else{
                logger.info("Found "+tableName+". Checking if it has a unique column.");
                ResultSet rs = null;
                try {
                    rs = con.getMetaData().getColumns(null, null, tableName, "UNIQUES");
                    if (rs.next()) {
                        logger.info(tableName + " already has a uniques column.");
                    } else {
                        logger.info("Detected no uniques column in " + tableName);
                        sql = "ALTER TABLE " + tableName + " ADD COLUMN UNIQUES INT NOT NULL DEFAULT 0";
                        PreparedStatement ps = null;
                        try {
                            ps = con.prepareStatement(sql);
                            ps.execute();
                        }
                        finally{
                            DbUtilities.closeDatabaseObjects(ps, null);
                        }
                    }
                }
                finally{
                    DbUtilities.closeDatabaseObjects(null, rs);
                }
            }
            tableName = "ObjectiveTimers";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName+" table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE "+tableName+" (ID VARCHAR(30) NOT NULL DEFAULT 'Unknown', TIMER LONG NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try {
                    ps = con.prepareStatement(sql);
                    ps.execute();
                }
                finally{
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
                Connection dbcon = ModSupportDb.getModSupportDb();
                try {
                    ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"DEPOT\", 0)");
                    ps.executeUpdate();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                finally{
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }

                dbcon = ModSupportDb.getModSupportDb();
                try {
                    ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(\"TITAN\", 0)");
                    ps.executeUpdate();
                }
                catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                finally{
                    DbUtilities.closeDatabaseObjects(ps, null);
                    DbConnector.returnConnection(dbcon);
                }
            }
            SupplyDepots.initializeDepotTimer();
            Titans.initializeTitanTimer();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbConnector.returnConnection(con);
        }
    }
}
