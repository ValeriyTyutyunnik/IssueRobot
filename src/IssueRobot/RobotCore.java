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

import java.io.Console;
import static java.lang.Integer.max;
import java.util.ArrayList;
import javafx.util.Pair;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;

public class RobotCore {
    ArrayList<Project> projects = new ArrayList<Project>();

    private boolean ConsoleAuthDataPrompt = true;
    private JiraClient jira;

    static boolean isRouting = false;

    final String load_fields = "summary,description,assignee,reporter,comment,labels";
    static String jira_url = "";
    static int jira_max_results = 50;
    static int robot_period = 0;
    
    static String login = "";
    static char pwd[];
    
    /** print help and exit
     */
    public void printHelp() {
        StringBuilder sb = new StringBuilder(1000);
        sb.append("Issue Robot is an application for advanced assignment of performers to the JIRA issue.");
        sb.append(Logging.crlf).append("For more information please visit https://github.com/ValeriyTyutyunnik/IssueRobot");
        sb.append(Logging.crlf).append(Logging.crlf);
        sb.append("Ussage:").append(Logging.crlf);
        sb.append("  IssueRobot.exe [-h] | [-s] [-c <file.xml>] [-l <login password> | -a]").append(Logging.crlf);
        sb.append("Params:").append(Logging.crlf);
        sb.append("  -h                   Print help and exit").append(Logging.crlf);
        sb.append("  -c <file.xml>        Use XML configuration from file <file.xml>").append(Logging.crlf);
        sb.append("                       By default the program will try to load the configuration").append(Logging.crlf);
        sb.append("                       from the config.xml file").append(Logging.crlf);
        sb.append("  -l <login password>  Jira authorization data. If the parameter is not passed,").append(Logging.crlf);
        sb.append("                       the program will try to find the data in the configuration file").append(Logging.crlf);
        sb.append("                       If they are not there either, you will be prompted to enter").append(Logging.crlf);
        sb.append("                       data in the console").append(Logging.crlf);
        sb.append("  -a                   Load without JIRA authorization. Use this key when you need just to load").append(Logging.crlf);
        sb.append("                       issues for some analize and checks. Issue routing will be disable").append(Logging.crlf);
        sb.append("                       Warning: Your JIRA have to be open to unauthorized requests.").append(Logging.crlf);
        sb.append("  -s                   Silent mode. No progress logging in console. But errors will be displayed").append(Logging.crlf);
        Logging.SilentOff = true; // print help even if silent is on
        Logging.log(sb.toString());
        System.exit(0);
    }
    
    /** prints basic cofiguration in console
     */
    private void logSettings() {
        StringBuilder sb = new StringBuilder(500);
        sb.append("Basic configuration:").append(Logging.crlf);
        sb.append("  Jira server = ").append(jira_url).append(Logging.crlf);
        sb.append("  Routing = ").append(isRouting).append(Logging.crlf);
        for (int i=0; i < projects.size(); i++) {
            sb.append("  Project №").append(i+1).append(": ").append(projects.get(i).Name).append(Logging.crlf);
            sb.append("      jql = ").append(projects.get(i).jql).append(Logging.crlf);
            if (!projects.get(i).add_labels.isEmpty()) {
                sb.append("      add labels = ").append(projects.get(i).add_labels.toString().replace("[", "").replace("]", ""));
                sb.append(Logging.crlf);
            }
            if (!projects.get(i).EditFields.isEmpty()) {
                for (Pair<String, String> edits: projects.get(i).EditFields) {
                    sb.append("      edit fields = ").append(edits.getKey()).append(" => ").append(edits.getValue());
                    sb.append(Logging.crlf);
                }
            }
            if (!"".equals(projects.get(i).add_comment)) {
                sb.append("      add comment = ").append(projects.get(i).add_comment).append(Logging.crlf);
            }
            for (int j=0; j < projects.get(i).blocks.size(); j++) {
                sb.append("        Block №").append(j+1).append(": ").append(projects.get(i).blocks.get(j).BlockName).append(Logging.crlf);
                sb.append("          Assignees: ");
                sb.append(projects.get(i).blocks.get(j).Assignees.toString().replace("[", "").replace("]", ""));
                sb.append(Logging.crlf);
                
                if (!projects.get(i).blocks.get(j).EditFields.isEmpty()) {
                    for (Pair<String, String> edits: projects.get(i).blocks.get(j).EditFields) {
                        sb.append("          edit fields = ").append(edits.getKey()).append(" => ").append(edits.getValue());
                        sb.append(Logging.crlf);
                    }
                }
            }
        }
        sb.append("************************************************************").append(Logging.crlf);
        Logging.log(sb.toString());
    }
    
    /** Main fuctuion. Checks JIRA issues and makes changes if routing on
     */
    private void execute() {
        if (RobotCore.isRouting && !Timetable.isWorkingDay()) { 
            Logging.PrintProgress("Not working now");
            return;
        }
        projects.forEach((project) -> {
            try {
                int StartAt = 0;
                do {
                    Logging.PrintProgress("Load issues for project " + project.Name);
                    project.SearchResult = jira.searchIssues(project.jql,
                                                             load_fields,
                                                             jira_max_results,
                                                             StartAt);
                    if (project.SearchResult.total > 0) {
                        project.CheckAndAssignIssues();
                    }
                    // if routing on then total result will decrease, so it's need to start at 0 all iterations
                    if (!RobotCore.isRouting || !project.changing_without_routing) {
                        StartAt += jira_max_results;
                    }
                } while (project.SearchResult.total > max(StartAt, jira_max_results));

                project.SearchResult = null;
            } catch (JiraException ex) {
                Logging.PrintErr("Failed JIRA request in project " + project.Name + ": " + ex.toString());
                project.SearchResult = null;
            }
        });
        Logging.ClearLine();
    }    
    
    /** Checks all projects and removes non-validated projects.
     *  Reason will be prompted in console
     */
    void validateProjects() {
        for (int i=projects.size()-1; i >=0; i--) {
            if (!projects.get(i).isValid()) {
                Logging.PrintWarn("Removing project " + projects.get(i).Name);
                projects.remove(i);
            }
        }
    }
    
    /** Authorization through the console when auth pair 
     * wasn't passed through the parameters or XML config
     */
    public void Auth() {
        
        Console console = System.console();
        if (console == null) {
            Logging.PrintErr("Couldn't get Console instance");
            System.exit(0);
        }
        
        login = console.readLine("JIRA login: ");
        pwd = console.readPassword("JIRA password: ");
    }
    
    /** prints simple progress string in concole between executions
     */
    private void wait_to_next_work(int time) {
        String simple_animation= "|/-\\";
        for (int x = time/100 ; x > 0 ; x--) {
            String str = " " + simple_animation.charAt(x % simple_animation.length())
                         + " next execution will start at " + x/10 + " sec";
            Logging.PrintProgress(str);
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
                Logging.PrintErr("Process interupted " + ex.toString());
            }
        }
        Logging.ClearLine();
    }
        
    @SuppressWarnings({"AssignmentToForLoopParameter"})
    public static void main(String agrs[]) {
        RobotCore Robot = new RobotCore();

        if (agrs.length > 0) {
            for (int i=0; i < agrs.length; i++) {
                try {
                    switch (agrs[i].trim()) {
                        case "-c":
                            i++;
                            Configuration.config_file = agrs[i].trim();
                            break;
                        case "-l":
                            i++;
                            RobotCore.login = agrs[i].trim();
                            i++;
                            RobotCore.pwd = agrs[i].trim().toCharArray();
                            break;
                        case "-h":
                            Robot.printHelp();
                            break;
                        case "-a":
                            Robot.ConsoleAuthDataPrompt = false;
                            break;
                        case "-s":
                            Logging.SilentOff = false; // turns on silent
                            break;
                        default:
                            Logging.PrintErr("Unknown argument: " + agrs[i].trim());
                            Robot.printHelp();
                            break;
                    }
                } catch (Exception ex) {
                    Logging.PrintErr("Arguments parsing error: " + ex.toString());
                    Robot.printHelp();
                }
            }
        }
        try {
            Robot.projects = Configuration.load_xml_config();
        } catch (Exception ex) {
            Logging.PrintErr("XML configuration parsing error: " + ex.toString());
            System.exit(0);
        }

        if ("".equals(RobotCore.login) && Robot.ConsoleAuthDataPrompt) {
            Robot.Auth();
        }
        
        if (Robot.ConsoleAuthDataPrompt && !"".equals(login)) {
            Robot.jira = new JiraClient(jira_url, new BasicCredentials(RobotCore.login, new String (RobotCore.pwd)));
            RobotCore.pwd = new char[] {0,0,0,0};
        } else {
            Robot.jira = new JiraClient(jira_url, null);
            RobotCore.isRouting = false;
        }
        Robot.validateProjects();
        Robot.logSettings();
        
        if (Robot.projects.size() > 0 ) {
            do {
                Robot.execute();
                
                // No cycling for test using
                if (!RobotCore.isRouting) {
                    break;
                }
                Robot.wait_to_next_work(Timetable.getNextStartTime());
            } while (Timetable.robot_period > 0);
        } else {
            Logging.PrintErr("No projects in configuration");
        }
    }
        
}
