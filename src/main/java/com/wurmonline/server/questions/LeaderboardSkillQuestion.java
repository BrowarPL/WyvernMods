package com.wurmonline.server.questions;

import com.wurmonline.server.DbConnector;
import com.wurmonline.server.creatures.Creature;
import com.wurmonline.server.deities.Deities;
import com.wurmonline.server.skills.SkillList;
import com.wurmonline.server.utils.DbUtilities;
import net.coldie.tools.BmlForm;
import org.gotti.wurmunlimited.modsupport.ModSupportDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class LeaderboardSkillQuestion extends Question {
    protected int skillNum;
    protected HashMap<String, Integer> optIn = new HashMap<>();

    public LeaderboardSkillQuestion(Creature aResponder, String aTitle, String aQuestion, long aTarget, int skillNum){
        super(aResponder, aTitle, aQuestion, 79, aTarget);
        this.skillNum = skillNum;
    }

    @Override
    public void answer(Properties answer) {
        boolean accepted = "true".equals(answer.getProperty("okay"));
        if (accepted) {
            LeaderboardQuestion lbq = new LeaderboardQuestion(this.getResponder(), "Leaderboard", "Which leaderboard would you like to view?", this.getResponder().getWurmId());
            lbq.sendQuestion();
        }
    }

    public int[] getSkillLevelColors(double skill){
        int[] colors = new int[3];
        if (Double.isNaN(skill) || Double.isInfinite(skill)) {
            skill = 0d;
        }
        skill = Math.max(0d, Math.min(100d, skill));

        if(skill >= 90){
            double percentTowards100 = 1 - ((100 - skill) * 0.1);
            double greenPower = 128 + (128 * percentTowards100);
            colors[1] = (int) Math.min(255, Math.max(0, greenPower));
            colors[2] = (int) Math.max(0, Math.min(255, 255 - greenPower));
        }else if(skill >= 50){
            double percentTowards90 = 1 - ((90 - skill) * 0.025);
            double greenPower = percentTowards90 * 128;
            colors[1] = (int) Math.max(128, Math.min(255, greenPower));
            colors[2] = (int) Math.min(255, Math.max(0, 255 - greenPower));
        }else{
            double percentTowards50 = 1 - ((50 - skill) * 0.02);
            double otherPower = 255 - (percentTowards50 * 255);
            colors[0] = (int) Math.min(255, Math.max(0, otherPower));
            colors[1] = (int) Math.min(255, Math.max(128, otherPower));
            colors[2] = 255;
        }
        return colors;
    }

    protected void identifyOptIn(){
        optIn.clear();
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

    @Override
    public void sendQuestion() {
        BmlForm f = new BmlForm("");
        f.addHidden("id", String.valueOf(this.id));
        ArrayList<String> names = new ArrayList<>();
        ArrayList<Double> skills = new ArrayList<>();
        ArrayList<Integer> deities = new ArrayList<>();
        String name;
        double skill;
        int deity;

        identifyOptIn();

        Connection dbcon = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            dbcon = DbConnector.getPlayerDbCon();
            ps = dbcon.prepareStatement("SELECT players.name, skills.value, players.deity FROM skills JOIN players ON skills.owner = players.wurmid WHERE skills.number = ? AND (players.power = 0) ORDER BY skills.value DESC LIMIT 20");
            ps.setInt(1, skillNum);
            rs = ps.executeQuery();
            while(rs.next()){
                name = rs.getString(1);
                skill = rs.getDouble(2);
                deity = rs.getInt(3);
                names.add(name);
                skills.add(skill);
                deities.add(deity);
            }
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally{
            DbUtilities.closeDatabaseObjects(ps, rs);
            DbConnector.returnConnection(dbcon);
        }

        f.addBoldText("Top 20 players in " + this.getQuestion());
        f.addText("\n\n");
        DecimalFormat df = new DecimalFormat(".000");

        for (int i = 0; i < names.size() && i < skills.size(); i++) {
            name = names.get(i);
            String extra = "";

            if(!optIn.containsKey(name) || optIn.get(name).equals(0)){
                name = "Unknown";
            }
            if(skillNum == SkillList.CHANNELING){
                extra = " (" + Deities.getDeityName(deities.get(i)) + ")";
            }

            int[] color = getSkillLevelColors(skills.get(i));
            String rowText = df.format(skills.get(i)) + " - " + name + extra;

            if(names.get(i).equals(this.getResponder().getName())){
                rowText = df.format(skills.get(i)) + " - " + names.get(i) + extra;
                f.addBoldColoredText(rowText, color[0], color[1], color[2]);
            }else{
                f.addColoredText(rowText, color[0], color[1], color[2]);
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