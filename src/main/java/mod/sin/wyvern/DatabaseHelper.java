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

    private static boolean hasPlayerRow(Connection dbcon, String tableName, String columnName, String playerName) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement("SELECT 1 FROM " + tableName + " WHERE " + columnName + " = ? LIMIT 1");
            ps.setString(1, playerName);
            rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
    }

    private static void insertLeaderboardOpt(Connection dbcon, String playerName) {
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement("INSERT INTO LeaderboardOpt (name) VALUES(?)");
            ps.setString(1, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
        }
    }

    private static void insertPlayerStats(Connection dbcon, String playerName) {
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement("INSERT INTO PlayerStats (NAME) VALUES(?)");
            ps.setString(1, playerName);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtilities.closeDatabaseObjects(ps, null);
        }
    }

    private static void ensureObjectiveTimerRow(Connection dbcon, String id) {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement("SELECT 1 FROM ObjectiveTimers WHERE ID = ? LIMIT 1");
            ps.setString(1, id);
            rs = ps.executeQuery();
            if (!rs.next()) {
                DbUtilities.closeDatabaseObjects(ps, rs);
                ps = null;
                rs = null;

                ps = dbcon.prepareStatement("INSERT INTO ObjectiveTimers (ID, TIMER) VALUES(?, ?)");
                ps.setString(1, id);
                ps.setLong(2, 0L);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DbUtilities.closeDatabaseObjects(ps, rs);
        }
    }

    public static void onPlayerLogin(Player player){
        Connection dbcon = ModSupportDb.getModSupportDb();
        try {
            boolean foundLeaderboardOpt = hasPlayerRow(dbcon, "LeaderboardOpt", "name", player.getName());
            if (!foundLeaderboardOpt) {
                logger.info("No leaderboard entry for " + player.getName() + ". Creating one.");
                insertLeaderboardOpt(dbcon, player.getName());
            }

            boolean foundPlayerStats = hasPlayerRow(dbcon, "PlayerStats", "NAME", player.getName());
            if (!foundPlayerStats) {
                logger.info("No player stats entry for " + player.getName() + ". Creating one.");
                insertPlayerStats(dbcon, player.getName());
            }
        } finally {
            DbConnector.returnConnection(dbcon);
        }
    }

    public static void onServerStarted(){
        Connection con = ModSupportDb.getModSupportDb();
        try {
            String sql;
            String tableName = "LeaderboardOpt";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName + " table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE " + tableName + " (name VARCHAR(30) NOT NULL DEFAULT 'Unknown', OPTIN INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try {
                    ps = con.prepareStatement(sql);
                    ps.execute();
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }

            tableName = "SteamIdMap";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName + " table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE " + tableName + " (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', STEAMID LONG NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try{
                    ps = con.prepareStatement(sql);
                    ps.execute();
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }

            tableName = "PlayerStats";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName + " table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE " + tableName + " (NAME VARCHAR(30) NOT NULL DEFAULT 'Unknown', KILLS INT NOT NULL DEFAULT 0, DEATHS INT NOT NULL DEFAULT 0, DEPOTS INT NOT NULL DEFAULT 0, HOTAS INT NOT NULL DEFAULT 0, TITANS INT NOT NULL DEFAULT 0, UNIQUES INT NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try {
                    ps = con.prepareStatement(sql);
                    ps.execute();
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }else{
                logger.info("Found " + tableName + ". Checking if it has a unique column.");
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
                        } finally {
                            DbUtilities.closeDatabaseObjects(ps, null);
                        }
                    }
                } finally {
                    DbUtilities.closeDatabaseObjects(null, rs);
                }
            }

            tableName = "ObjectiveTimers";
            if (!ModSupportDb.hasTable(con, tableName)) {
                logger.info(tableName + " table not found in ModSupport. Creating table now.");
                sql = "CREATE TABLE " + tableName + " (ID VARCHAR(30) NOT NULL DEFAULT 'Unknown', TIMER LONG NOT NULL DEFAULT 0)";
                PreparedStatement ps = null;
                try {
                    ps = con.prepareStatement(sql);
                    ps.execute();
                } finally {
                    DbUtilities.closeDatabaseObjects(ps, null);
                }
            }

            ensureObjectiveTimerRow(con, "DEPOT");
            ensureObjectiveTimerRow(con, "TITAN");

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