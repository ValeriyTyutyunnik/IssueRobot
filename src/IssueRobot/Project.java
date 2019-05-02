package IssueRobot;

/* MIT License

Copyright (c) 2019 Tyutyunnik Valeriy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javafx.util.Pair;
import net.rcarz.jiraclient.Field;
import net.rcarz.jiraclient.Issue;
import net.rcarz.jiraclient.JiraException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Project {
    String Name;
    
    String jql = "";
    Issue.SearchResult SearchResult;
    
    String add_comment = "";
    HashSet<String> add_labels = new HashSet<String>();
    // change custumer field value in project
    ArrayList<Pair<String, String>> EditFields  = new ArrayList<Pair<String, String>>();

    boolean always_parse_comments = false;
    boolean reporter_may_be_assignee = false;
    boolean log_enable = true;
    boolean changing_without_routing = true;
    
    String tomitaConfig = "config.proto";
    String TomitaFactField = "Fact";
    
    ArrayList<Block> blocks = new ArrayList<Block>();
    HashSet<String> facts = new HashSet<String>();
    HashSet<String> comment_facts = new HashSet<String>();
    
    String blocks_points = "";

    public Project (String name) {
        this.Name = name;
    }
    
    /** Checks the project and writes the reason why the project is not valid
     * @return {boolean}
     */
    public boolean isValid() {
        boolean result = true;
        if ("".equals(jql)) {
            Logging.PrintWarn("JQL is empty in project " + Name);
            result = false;
        } 
        
        if (!blocks.isEmpty()) {
            for (int i=blocks.size()-1; i >= 0; i--) {
                if (blocks.get(i).Assignees.isEmpty()) {
                    Logging.PrintWarn("Block " + blocks.get(i).BlockName + " removed from project " 
                                      + Name + " due empty assignees list");
                    blocks.remove(i);
                }
            }
        }

        if (blocks.isEmpty()) {
            if ("".equals(add_comment) && add_labels.isEmpty() && EditFields.isEmpty()) {
                Logging.PrintWarn("No blocks configuration, or changing labels, comment, or fields in the project " + Name);
                result = false;
            } else {
                changing_without_routing = false;
            }
        } else if (blocks.size() > 1) { // No need to call tomita for only one project
            try {
                Tomita Tomita = new Tomita(tomitaConfig, TomitaFactField);
                facts = Tomita.parseFactsFromText("Just test");
            } catch (Exception ex) {
                Logging.PrintErr("Tomita parser failed. Project " + Name 
                                 + " will be removed until error is fixed. " + Logging.crlf 
                                 + ex.toString());
                result = false;
            }
        }
        return result;
    }
    
    /** returns text from Issue summary and description 
     * @return {String}
     */
    private String get_parse_text (Issue issue) {
        StringBuilder builder = new StringBuilder(200);
        if (issue.getSummary() != null && !"".equals(issue.getSummary().trim())) {
            builder.append(issue.getSummary().trim());
            builder.append(".").append(Logging.crlf);
        }
        if (issue.getDescription() != null && !"".equals(issue.getDescription().trim()))  {
            builder.append(issue.getDescription().trim());
            builder.append(".").append(Logging.crlf);
        }
        return builder.toString();
    }
    
    /** returns text from Issue comments 
     * @return {String}
     */
    private String get_parse_comment (Issue issue) {
        StringBuilder builder = new StringBuilder(200);
        
        JSONObject comments = (JSONObject) issue.getField("comment");
        if (comments != null) {
            JSONArray comment = comments.getJSONArray("comments");
            for (int j = 0; j < comment.size(); j++) {
                builder.append(comment.getJSONObject(j).getString("body").trim());
                builder.append(".").append(Logging.crlf);
            }
        }
        return builder.toString();
    }
    
    /**
     * Returns the index of the block to which the issue should be assigned.
     * If several blocks have the same weight - returns random among them.
     * @param issue
     * @return {int} block index
     */
    public int findBlockToRoute(Issue issue) {
        
        if (blocks.size() == 1) {
            return 0;
        }
               
        try {
            Tomita Tomita = new Tomita(tomitaConfig, TomitaFactField);
            facts = Tomita.parseFactsFromText(get_parse_text(issue));
            
            // if facts wasn't found in description or comments always have to be parsed
            if (always_parse_comments || facts.isEmpty()) {
                comment_facts = Tomita.parseFactsFromText(get_parse_comment(issue));
            }
        } catch (Exception ex) {
            Logging.PrintErr("Project " + Name + ". Tomitaparser failed: " + ex.toString());
        }
        
        int block_num, points, max_points = 0;
        ArrayList<Integer> LeadBlocks = new ArrayList<Integer>();

        for( int i=0; i < this.blocks.size(); i++) {
            points = this.blocks.get(i).checkTicketKeywords(facts);
            points += this.blocks.get(i).checkTicketKeywords(comment_facts);
            points += this.blocks.get(i).checkTicketLabels(issue.getLabels());
            if (issue.getReporter() != null) {
                points += this.blocks.get(i).checkTicketReporter(issue.getReporter().getName());
            }
            if (i == 0) {
                max_points = points;
                blocks_points = "";
            }
            if (log_enable) {
                blocks_points += "".equals(blocks_points) ? "" : ", ";
                blocks_points += this.blocks.get(i).BlockName + '(' + points + ')';
            }

            if (points > max_points){
                LeadBlocks.clear();
                LeadBlocks.add(i);
                max_points = points;
            } else if (points == max_points) {
                LeadBlocks.add(i);
            }
        }
        
        if (LeadBlocks.size() > 1) {
            Random randNumber = new Random();
            block_num =  LeadBlocks.get(randNumber.nextInt(LeadBlocks.size()));
        } else {
            block_num = LeadBlocks.get(0);
        }
        
        return block_num;
    }
    
    /**
     * Makes changes from the project configuration - adds labels, comments, changes fields
     * @param issue 
     */
    public void MakeChangesToIssue (Issue issue) {
        if (RobotCore.isRouting) {
            try {
                if (add_labels.size() > 0) {
                    for (String label : add_labels) {
                        if (!issue.getLabels().contains(label)) {
                                issue.update().fieldAdd(Field.LABELS, label).execute();
                       }
                    }
                }

                if (!"".equals(add_comment)) {
                    issue.addComment(add_comment);
                }
                
                for (Pair<String, String> edits: EditFields) {
                    issue.update().field(edits.getKey(), edits.getValue()).execute();
                }
            } catch (JiraException ex) {
                Logging.PrintErr("Failed Jira request for project changes in issue " + issue.getKey() + ": " + ex.toString());
            }
        }
    }
    
    /**
     * Defines a block for all found issues and assigns an executor of this block if routing on. 
     */
    public void CheckAndAssignIssues() {
        int cnt = 0;
        for (Issue issue : SearchResult.issues) {
            cnt++;
            Logging.PrintProgress("Project " + Name + " [" + (SearchResult.start+cnt) + '/' + SearchResult.total
                                    + "] Check issue " + issue.getKey());
            // if need to change executor, not just add_comment or label
            if (blocks.size() > 0 ) {
                int next_block = findBlockToRoute(issue);
                Block block = blocks.get(next_block);
                String new_assignee = block.getNextAssignee();

                if (reporter_may_be_assignee && issue.getReporter() != null
                    && issue.getReporter().getName().equals(new_assignee)) {
                    new_assignee = block.switch_executor();
                }

                if (log_enable) {
                    Logging.logIssueDetails(Name, 
                                            issue.getKey(),
                                            issue.getAssignee() != null ? issue.getAssignee().getName() : "",
                                            issue.getReporter() != null ? issue.getReporter().getName() : "",
                                            block.BlockName,
                                            new_assignee,
                                            blocks_points,
                                            issue.getSummary(),
                                            issue.getLabels().toString().replace("[", "").replace("]", ""),
                                            facts.toString().replace("[", "").replace("]", ""),
                                            comment_facts.toString().replace("[", "").replace("]", ""));
                }
                if (RobotCore.isRouting) {
                        Logging.PrintProgress("Project " + Name + " [" + (SearchResult.start+cnt) + '/' + SearchResult.total
                                + "] Assign issue " + issue.getKey());
                        
                    try {
                        issue.update().field(Field.ASSIGNEE, new_assignee).execute();
                        for (Pair<String, String> edits: block.EditFields) {
                            issue.update().field(edits.getKey(), edits.getValue().replace("$new_assignee", new_assignee))
                                 .execute();
                        }
                    } catch (JiraException ex) {
                        Logging.PrintErr("Failed Jira request for blocks changes in issue " + issue.getKey() + ": " + ex.toString());
                    }
                }
            }
            MakeChangesToIssue(issue); // add labels, comment here if need
        }
    }

}
