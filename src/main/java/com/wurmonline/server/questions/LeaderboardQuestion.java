package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.Servers;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.players.AchievementTemplate;
import com.wurmonline.server.skills.SkillSystem;
import com.wurmonline.server.skills.SkillTemplate;
import com.wurmonline.server.utils.DbUtilities;
import mod.sin.wyvern.AchievementChanges;
import net.coldie.tools.BmlForm;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class LeaderboardQuestion extends Question {

    public LeaderboardQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
    }
    protected void setPlayerOptStatus(String name, int opt){
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        try {
            ps = dbcon.prepareStatement("UPDATE LeaderboardOpt SET OPTIN = " + opt + " WHERE name = \"" + name + "\"");
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
    @Override
    public void answer(Properties answer) {
        boolean skill = answer.containsKey("accept") && answer.get("accept") == "true";
        boolean achievements = answer.containsKey("achievements") && answer.get("achievements") == "true";
        boolean custom = answer.containsKey("custom") && answer.get("custom") == "true";
        if (skill) {
            int entry = Integer.parseInt(answer.getProperty("leaderboard"));
            String val = skillMap.get(entry);
            int skillNum = skillIdMap.get(entry);
            LeaderboardSkillQuestion lbsq = new LeaderboardSkillQuestion(this.getResponder(), "Leaderboard", val, this.getResponder().getWurmId(), skillNum);
            lbsq.sendQuestion();
        }else if(achievements){
            int entry = Integer.parseInt(answer.getProperty("achievementboard"));
            String val = achievementMap.get(entry);
            int achievementNum = achievementIdMap.get(entry);
            LeaderboardAchievementQuestion lbaq = new LeaderboardAchievementQuestion(this.getResponder(), "Leaderboard", val, this.getResponder().getWurmId(), achievementNum);
            lbaq.sendQuestion();
        }else if(custom){
            int entry = Integer.parseInt(answer.getProperty("customboard"));
            String val = customMap.get(entry);
            LeaderboardCustomQuestion lbcq = new LeaderboardCustomQuestion(this.getResponder(), "Leaderboard", val, this.getResponder().getWurmId(), entry);
            lbcq.sendQuestion();
        }else{
            String name = this.getResponder().getName();
            if(answer.containsKey("optin") && answer.get("optin") == "true"){
                logger.info("Player "+name+" has opted into Leaderboard system.");
                setPlayerOptStatus(name, 1);
                this.getResponder().getCommunicator().sendNormalServerMessage("You have opted into the Leaderboard system!");
            }else if(answer.containsKey("optout") && answer.get("optout") == "true"){
                logger.info("Player "+name+" has opted out of the Leaderboard system.");
                setPlayerOptStatus(name, 0);
                this.getResponder().getCommunicator().sendNormalServerMessage("You have opted out of the Leaderboard system.");
            }
        }
    }

    protected HashMap<Integer, String> skillMap = new HashMap<>();
    protected HashMap<Integer, Integer> skillIdMap = new HashMap<>();
    protected HashMap<Integer, String> customMap = new HashMap<>();
    public String getSkillOptions(){
        StringBuilder builder = new StringBuilder();
        SkillTemplate[] skillTemplates = SkillSystem.getAllSkillTemplates();
        Arrays.sort(skillTemplates, Comparator.comparing(SkillTemplate::getName));
        skillMap.clear();
        for(int i = 0; i < skillTemplates.length; ++i){
            builder.append(skillTemplates[i].getName());
            skillMap.put(i, skillTemplates[i].getName());
            skillIdMap.put(i, skillTemplates[i].getNumber());
            if(i != skillTemplates.length-1){
                builder.append(',');
            }
        }
        return builder.toString();
    }

    protected HashMap<Integer, String> achievementMap = new HashMap<>();
    protected HashMap<Integer, Integer> achievementIdMap = new HashMap<>();
    public String getAchievementOptions(){
        StringBuilder builder = new StringBuilder();
        Collection<AchievementTemplate> achievements = AchievementChanges.goodAchievements.values();
        List<AchievementTemplate> sortedAchievements = new ArrayList<>(achievements);
        sortedAchievements.sort(Comparator.comparing(AchievementTemplate::getName));
        achievementMap.clear();
        for(int i = 0; i < sortedAchievements.size(); ++i){
            builder.append(sortedAchievements.get(i).getName());
            achievementMap.put(i, sortedAchievements.get(i).getName());
            achievementIdMap.put(i, sortedAchievements.get(i).getNumber());
            if(i != sortedAchievements.size()-1){
                builder.append(',');
            }
        }
        return (builder.toString()).replaceAll("'", "");
    }
    public String getCustomOptions(){
        StringBuilder builder = new StringBuilder("Total Skill");
        customMap.put(0, "Total Skill");
        builder.append(",High Skills");
        customMap.put(1, "High Skills");
        builder.append(",Most Titles");
        customMap.put(2, "Most Titles");
        builder.append(",Uniques Slain");
        customMap.put(3, "Uniques Slain");
        builder.append(",Titans Slain");
        customMap.put(4, "Titans Slain");
        builder.append(",Most Affinities");
        customMap.put(5, "Most Affinities");
        builder.append(",Most Unique Achievements");
        customMap.put(6, "Most Unique Achievements");
        builder.append(",Largest Structures");
        customMap.put(7, "Largest Structures");
        builder.append(",Most Populated Villages");
        customMap.put(8, "Most Populated Villages");
        if(Servers.localServer.PVPSERVER || this.getResponder().getPower() >= 5){
            builder.append(",PvP Kills");
            customMap.put(9, "PvP Kills");
            builder.append(",PvP Deaths");
            customMap.put(10, "PvP Deaths");
            builder.append(",Depots Captured");
            customMap.put(11, "PvP Depots Captured");
        }
        return builder.toString();
    }
    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));
        int opted;
        Connection dbcon = ModSupportDb.getModSupportDb();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = dbcon.prepareStatement("SELECT * FROM LeaderboardOpt WHERE name = \"" + this.getResponder().getName() + "\"");
            rs = ps.executeQuery();
            opted = rs.getInt("OPTIN");
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }
        f.addBoldText("You are currently " + (opted == 0 ? "not " : "") + "opted into the leaderboard system.\n\n");
        f.addBoldText("Skill Leaderboards");
        f.addRaw("harray{label{text='View leaderboard:'}dropdown{id='leaderboard';options='");
        f.addRaw(getSkillOptions());
        f.addRaw("'}}");
        f.beginHorizontalFlow();
        f.addButton("Accept", "accept");
        f.endHorizontalFlow();
        f.addText(" \n\n");
        f.addBoldText("Achievement Leaderboards");
        f.addRaw("harray{label{text='View leaderboard:'}dropdown{id='achievementboard';options='");
        f.addRaw(getAchievementOptions());
        f.addRaw("'}}");
        f.beginHorizontalFlow();
        f.addButton("Accept", "achievements");
        f.endHorizontalFlow();
        f.addText(" \n\n");
        f.addBoldText("Special Leaderboards");
        f.addRaw("harray{label{text='View leaderboard:'}dropdown{id='customboard';options='");
        f.addRaw(getCustomOptions());
        f.addRaw("'}}");
        f.beginHorizontalFlow();
        f.addButton("Accept", "custom");
        f.endHorizontalFlow();
        f.addText(" \n\n");
        f.addBoldText("Opt into or out of the Leaderboard system.");
        f.beginHorizontalFlow();
        f.addButton("Opt In", "optin");
        f.addButton("Opt Out", "optout");
        f.endHorizontalFlow();
        this.getResponder().getCommunicator().sendBml(400, 500, true, true, f.toString(), 150, 150, 200, this.title);
    }
}
