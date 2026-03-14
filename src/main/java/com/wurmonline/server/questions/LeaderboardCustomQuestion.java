package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.utils.DbUtilities;
import net.coldie.tools.BmlForm;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

public class LeaderboardCustomQuestion extends Question {
    protected int entryNum;

    public LeaderboardCustomQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int entryNum){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.entryNum = entryNum;
    }
    @Override
    public void answer(Properties answer) {
        boolean accepted = answer.containsKey("okay") && "true".equals(answer.get("okay"));
        if (accepted) {
            LeaderboardQuestion lbq = new LeaderboardQuestion(this.getResponder(), "Leaderboard", "Which leaderboard would you like to view?", this.getResponder().getWurmId());
            lbq.sendQuestion();
        }
    }
    protected HashMap<String, Integer> optIn = new HashMap<>();
    protected void identifyOptIn(){
        String name;
        int opted;
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt");
            rs = ps.executeQuery();
            while (rs.next()) {
                name = rs.getString("name");
                opted = rs.getInt("OPTIN");
                optIn.put(name, opted);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }

    protected ArrayList<String> names = new ArrayList<>();
    protected ArrayList<Double> values = new ArrayList<>();
    protected ArrayList<String> extra = new ArrayList<>();
    protected void totalSkills(int limit){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        double skill;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, achievements.counter FROM achievements JOIN players ON achievements.player = players.wurmid WHERE achievements.achievement = 371 AND players.power = 0 ORDER BY achievements.counter DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skill = rs.getDouble(2);
                names.add(name);
                values.add(skill);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected void topAnySkill(int limit){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        int skillNum;
        double skill;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, skills.number, skills.value FROM skills JOIN players ON skills.owner = players.wurmid WHERE players.power = 0 ORDER BY skills.value DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skillNum = rs.getInt(2);
                skill = rs.getDouble(3);
                names.add(name);
                values.add(skill);
                extra.add(SkillSystem.getNameFor(skillNum));
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    //select wurmid, count(*) from titles group by wurmid order by count(*) desc;
    protected void totalTitles(int limit){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        double skill;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, COUNT(titles.wurmid) as Count FROM titles JOIN players ON titles.wurmid = players.wurmid WHERE players.power = 0 GROUP BY titles.wurmid ORDER BY Count DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skill = rs.getDouble(2);
                names.add(name);
                values.add(skill);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected void topAffinities(int limit){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        double affinities;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, sum(affinities.number) as Count FROM affinities JOIN players ON affinities.wurmid = players.wurmid WHERE players.power = 0 GROUP BY players.name ORDER BY Count DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                affinities = rs.getDouble(2);
                names.add(name);
                values.add(affinities);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected void topUniqueAchievements(int limit){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        double achievements;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, count(*) AS theCount FROM achievements JOIN players ON achievements.player = players.wurmid WHERE players.power = 0 GROUP BY players.name ORDER BY theCount DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                achievements = rs.getDouble(2);
                names.add(name);
                values.add(achievements);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected HashMap<Long, String> structureNames = new HashMap<>();
    protected HashMap<Long, String> structurePlanners = new HashMap<>();
    protected HashMap<Long, Integer> structureWalls = new HashMap<>();
    protected HashMap<Long, Integer> structureFloors = new HashMap<>();
    protected void calculateStructureWalls(){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        long wurmid;
        int walls;
        String planner;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("SELECT structures.wurmid, structures.name, count(*), structures.planner FROM walls JOIN structures ON structures.wurmid = walls.structure WHERE structures.finished = 1 AND walls.state > 1 GROUP BY walls.structure ORDER BY count(*) DESC");
            rs = ps.executeQuery();
            while(rs.next()){
                wurmid = rs.getLong(1);
                name = rs.getString(2);
                walls = rs.getInt(3);
                planner = rs.getString(4);
                structureNames.put(wurmid, name);
                structurePlanners.put(wurmid, planner);
                structureWalls.put(wurmid, walls);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected void calculateStructureFloors(){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        long wurmid;
        int floors;
        String planner;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("SELECT structures.wurmid, structures.name, count(*), structures.planner FROM floors JOIN structures ON structures.wurmid = floors.structure WHERE structures.finished = 1 AND floors.state > 1 GROUP BY floors.structure ORDER BY count(*) DESC");
            rs = ps.executeQuery();
            while(rs.next()){
                wurmid = rs.getLong(1);
                name = rs.getString(2);
                floors = rs.getInt(3);
                planner = rs.getString(4);
                structureNames.put(wurmid, name);
                structurePlanners.put(wurmid, planner);
                structureFloors.put(wurmid, floors);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected void topStructureSizes(int limit){
        calculateStructureWalls();
        calculateStructureFloors();
        HashMap<Long, Integer> structureTotals = new HashMap<>();
        for(long wurmid : structureWalls.keySet()){
            structureTotals.put(wurmid, structureWalls.get(wurmid));
        }
        for(long wurmid : structureFloors.keySet()){
            if(structureTotals.containsKey(wurmid)){
                int current = structureTotals.get(wurmid);
                structureTotals.put(wurmid, current + structureFloors.get(wurmid));
            }else{
                structureTotals.put(wurmid, structureFloors.get(wurmid));
            }
        }
        LinkedList<Map.Entry<Long, Integer>> list = new LinkedList<>(structureTotals.entrySet());
        list.sort(Map.Entry.comparingByValue());
        Collections.reverse(list);
        HashMap<Long, Integer> sortedHashMap = new LinkedHashMap<>();
        for (Map.Entry<Long, Integer> entry : list) {
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        Set<Map.Entry<Long, Integer>> set2 = sortedHashMap.entrySet();
        int num = 0;
        for(Iterator<Map.Entry<Long, Integer>> it = set2.iterator(); it.hasNext() && num < limit; ++num) {
            Map.Entry<Long, Integer> me2 = it.next();
            long wurmId = me2.getKey();
            int value = me2.getValue();
            names.add(structureNames.get(wurmId));
            values.add((double) value);
            extra.add(structurePlanners.get(wurmId));
        }
    }
    protected void topPlayerStats(String statName, int limit){
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        double stat;
        try {
            ps = dbcon.prepareStatement("SELECT name, "+statName+" FROM PlayerStats ORDER BY "+statName+" DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                stat = rs.getDouble(2);
                names.add(name);
                values.add(stat);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    protected void mostCitizens(int limit){
        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String name;
        String mayor;
        double stat;
        try {
            dbcon = DbConnector.getZonesDbCon();
            ps = dbcon.prepareStatement("SELECT name, maxcitizens, mayor FROM villages WHERE disbanded = 0 ORDER BY maxcitizens DESC LIMIT "+limit);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                stat = rs.getInt(2);
                mayor = rs.getString(3);
                ++stat; // Add one citizen to account for mayor.
                names.add(name);
                values.add(stat);
                extra.add(mayor);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
    }
    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));

        // Populates HashMap with latest opt-in data.
        identifyOptIn();

        // Identify and execute correct list generation.
        boolean format = false;
        boolean ignoreOpt = false;
        int limit = 20;
        switch(entryNum){
            case 0:
                limit = 50;
                totalSkills(limit);
                break;
            case 1:
                limit = 50;
                topAnySkill(limit);
                format = true;
                break;
            case 2:
                limit = 50;
                totalTitles(limit);
                break;
            case 3:
                limit = 20;
                topPlayerStats("uniques", limit);
                ignoreOpt = true;
                break;
            case 4:
                limit = 20;
                topPlayerStats("titans", limit);
                ignoreOpt = true;
                break;
            case 5:
                limit = 20;
                topAffinities(limit);
                break;
            case 6:
                limit = 20;
                topUniqueAchievements(limit);
                break;
            case 7:
                limit = 20;
                topStructureSizes(limit);
                ignoreOpt = true;
                break;
            case 8:
                limit = 100;
                mostCitizens(limit);
                ignoreOpt = true;
                break;
            case 9:
                limit = 10;
                topPlayerStats("kills", limit);
                ignoreOpt = true;
                break;
            case 10:
                limit = 10;
                topPlayerStats("deaths", limit);
                ignoreOpt = true;
                break;
            case 11:
                limit = 10;
                topPlayerStats("depots", limit);
                ignoreOpt = true;
                break;
        }

        f.addBoldText("Top "+limit+" players in "+this.getQuestion());
        f.addText("\n\n");
        DecimalFormat df = new DecimalFormat(".000");
        if(!format){
            df = new DecimalFormat("#");
        }
        String name;
        String line;
        for(int i = 0; i < names.size() && i < values.size(); ++i){
            name = names.get(i);
            if(!ignoreOpt) {
                if (!optIn.containsKey(name)) {
                    name = "Unknown";
                } else if (optIn.get(name).equals(0)) {
                    name = "Unknown";
                }
            }
            line = df.format(values.get(i)) + " - " + name;
            if(extra.size() >= i+1){
                line = line + " ("+ extra.get(i)+")";
            }
            if(names.get(i).equals(this.getResponder().getName())){
                f.addBoldText(line);
            }else{
                f.addText(line);
            }
        }
        f.addText(" \n");
        f.beginHorizontalFlow();
        f.addButton("Ok", "okay");
        f.endHorizontalFlow();
        f.addText(" \n");
        this.getResponder().getCommunicator().sendBml(400, 500, true, true, f.toString(), 150, 150, 200, this.title);
    }
}
