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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;

public class Block {

    public Block (String name) {
        this.BlockName = name;
    }
    
    String BlockName;
    
    int BlockQueue = 0;

    ArrayList<String> Assignees = new ArrayList<String>();
    
    Map<String, Integer> Keys = new HashMap<String, Integer>();
    Map<String, Integer> Reporters = new HashMap<String, Integer>();
    Map<String, Integer> Labels = new HashMap<String, Integer>();
    
    // change custumer field value in block
    ArrayList<Pair<String, String>> EditFields  = new ArrayList<Pair<String, String>>();
    
    /** switch current and next executor in queue
     * @return {String} - next assigne
     */
    public String switch_executor() {
        if (Assignees.isEmpty()) {
            return null;
        } else if (Assignees.size() == 1) {
            return Assignees.get(0);
        }

        int indx;
        if (BlockQueue >= Assignees.size())
            BlockQueue = 0;
        // get next queue index
        if (BlockQueue + 1 >= Assignees.size()) {
            indx = 0;
        } else {
            indx = BlockQueue + 1;
        }

        String cur_assignee = Assignees.get(BlockQueue);
        
        Assignees.set(BlockQueue, Assignees.get(indx));
        Assignees.set(indx, cur_assignee);
        
        return Assignees.get(BlockQueue);
    }

    /**
     * returns next assignee in queue
     * @return {String}
     */
    public String getNextAssignee() {
        if (Assignees.isEmpty()) {
            return null;
        } else if (BlockQueue >= Assignees.size()) {
            BlockQueue = 0;
        }
    
        int bq = BlockQueue;
        BlockQueue++;
        return Assignees.get(bq);
    }
    
    /**
     * returns block weight by keywords
     * @return {int}
     */
    public int checkTicketKeywords(HashSet<String> facts) {
        int points = 0;
        if (facts != null && !Keys.isEmpty() && !facts.isEmpty()) {
            points = facts.stream().filter((keyword) -> (Keys.containsKey(keyword)))
                     .map((keyword) -> Keys.get(keyword)).reduce(points, Integer::sum);
        }
        return points;
    }
    
    /**
     * returns block weight by labels
     * @return {int}
     */
    public int checkTicketLabels(List<String> labels) {
        int points = 0;
        if (labels != null && !labels.isEmpty() && !Labels.isEmpty()) {
           
            points = labels.stream().filter((label) -> (Labels.containsKey(label)))
                    .map((label) -> Labels.get(label)).reduce(points, Integer::sum);
        }
       return points;
    } 
    
    /**
     * returns block weight by reporter
     * @return {int}
     */
    public int checkTicketReporter(String reporter)
    {
       int points = 0;
        if(reporter != null && !"".equals(reporter) && Reporters.containsKey(reporter)) {
            points += Reporters.get(reporter);
        }
       return points;
    } 
    
}
