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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import javafx.util.Pair;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Configuration {
    // base configuration file
    static String config_file = "config.xml";

    // loads xml configuration
    private static Document getDocument() throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setValidating(false);
        DocumentBuilder builder = f.newDocumentBuilder();
        Document doc = builder.parse(new File(config_file));
        doc.getDocumentElement().normalize();
        return doc;
    }
   
    /** returns node attribute's integer value or default value if attr doesn't exists
     * @param node checking node
     * @param attr_name - attribute name sought
     * @param default_value - default value that'll be returned if attr doesn't exists
     * @return attribute integer value
     */
    private static int getAttrInt(Node node, String attr_name, int default_value) {
        Node attr = node.getAttributes().getNamedItem(attr_name);
        if (attr != null && !"".equals(getText(attr))) {
            return Integer.parseInt(getText(attr));
        }
        return default_value; 
    }
    
    /** returns node's text content value
     * @param node checking node
     * @param attr_name - attribute name sought
     * @return attribute text content value or empty String if text doesn't exists
     */
    private static String getAttr(Node node, String attr_name) {
        String result = "";
        Node attr = node.getAttributes().getNamedItem(attr_name);
        if (attr != null && !"".equals(getText(attr))) {
            result = getText(attr);
        }
        return result; 
    }
    
    /** returns true if attr text equals "true"
     * @param node checking node
     * @param attr_name - attribute name sought
     */
    private static boolean getAttrBool(Node node, String attr_name) {
        return "true".equals(getAttr(node, attr_name).toLowerCase());
    }
    
    /** returns trimed text content
     * @param node checking node
     * @param attr_name - attribute name sought
     */
    private static String getText(Node node) {
        return node.getTextContent().trim();
    }

    public static ArrayList<Project> load_xml_config() throws Exception {
        ArrayList<Project> Projects = new ArrayList<Project>();

        Document doc;
        doc = getDocument();
        doc.getDocumentElement().normalize();

        Node config = doc.getDocumentElement();
        NodeList config_nodes = config.getChildNodes();

        for (int i = 0; i < config_nodes.getLength(); i++) {
            Node config_node = config_nodes.item(i);
            if (config_node.getNodeType() == Node.ELEMENT_NODE) {
                switch (config_node.getNodeName()) {
                    case "robot_period":
                        String tmp = getText(config_node);
                        if (!"".equals(tmp) && Integer.parseInt(tmp) >= 0) {
                            Timetable.robot_period = Integer.parseInt(tmp);
                        }
                        break;

                    case "routing_on":
                        RobotCore.isRouting = "true".equals(getText(config_node).toLowerCase());
                        break;
                    case "jira_url":
                        if (!"".equals(getText(config_node))) {
                            RobotCore.jira_url = getText(config_node);
                            RobotCore.jira_max_results = getAttrInt(config_node, "max_results", 50);
                            if ("".equals(RobotCore.login)) {
                                RobotCore.login = getAttr(config_node, "login");
                                RobotCore.pwd = getAttr(config_node, "pwd").toCharArray();
                            }
                        }
                        break;

                    case "calendar":
                        NodeList calendar = config_node.getChildNodes();
                        for (int j=0; j < calendar.getLength(); j++) {
                            if (calendar.item(j).getNodeType() == Node.ELEMENT_NODE
                                && !"".equals(getText(calendar.item(j)))) {
                                tmp = getText(calendar.item(j));
                                switch (calendar.item(j).getNodeName()) {
                                    case "work_days":
                                        Timetable.Days = tmp;
                                        break;
                                    case "start_time":
                                        int delim = tmp.indexOf(':');
                                        if (delim > 0 && tmp.length() > delim) {
                                            Timetable.START_DAY = Integer.parseInt(tmp.substring(0,delim));
                                            Timetable.START_MINUTES = Integer.parseInt(tmp.substring(delim+1,tmp.length()));
                                        }
                                        break;
                                    case "end_time":
                                        delim = tmp.indexOf(':');
                                        if (delim > 0 && tmp.length() > delim) {
                                            Timetable.END_DAY = Integer.parseInt(tmp.substring(0,delim));
                                            Timetable.END_MINUTES = Integer.parseInt(tmp.substring(delim+1,tmp.length()));
                                        }
                                        break;
                                } // end switch calendar
                            }
                        }
                        break;

                    case "projects":
                        NodeList projects = config_node.getChildNodes();
                        for (int j = 0; j < projects.getLength(); j++) {
                            Node project_node = projects.item(j);
                            if (project_node.getNodeType() == Node.ELEMENT_NODE) {
                                Project project = new Project(project_node.getNodeName());
                                Projects.add(project);
                                
                                project.always_parse_comments = getAttrBool(project_node, "always_parse_comments");
                                project.reporter_cannot_be_assigned = getAttrBool(project_node, "reporter_cannot_be_assigned");
                                project.log_enable = getAttrBool(project_node, "log_enable");
                                
                                NodeList project_conf = project_node.getChildNodes();

                                for (int k = 0; k < project_conf.getLength(); k++) {
                                    Node project_item = project_conf.item(k);
                                    if (project_item.getNodeType() == Node.ELEMENT_NODE) {
                                        switch (project_item.getNodeName()) {
                                            case "jql":
                                                if (!"".equals(getText(project_item))) {
                                                    project.jql = getText(project_item);
                                                }
                                                break;

                                            case "add_labels":
                                                NodeList jira_labels = project_item.getChildNodes();
                                                for (int l = 0; l < jira_labels.getLength(); l++) { 
                                                    Node label = jira_labels.item(l);
                                                    if (label.getNodeType() == Node.ELEMENT_NODE &&
                                                        !"".equals(getText(label))) {
                                                        project.add_labels.add(getText(label));
                                                    }
                                                }
                                                break;
                                            case "add_comment":
                                                if (!"".equals(getText(project_item))) {
                                                    project.add_comment = getText(project_item);
                                                }
                                                break;
                                            case "edit_fields":
                                                NodeList edit_fields = project_item.getChildNodes();
                                                for (int l = 0; l < edit_fields.getLength(); l++) {
                                                    Node field = edit_fields.item(l);
                                                    if (field.getNodeType() == Node.ELEMENT_NODE
                                                        && !"".equals( getText(field))) {
                                                            Pair<String, String> edits = new Pair<String, String>(field.getNodeName(), getText(field));
                                                            project.EditFields.add(edits);
                                                    }
                                                }
                                                break;
                                            case "tomita_config":
                                                if (!"".equals(getText(project_item))) {
                                                    project.tomitaConfig = getText(project_item);

                                                    Node attr = project_item.getAttributes().getNamedItem("fact_field");
                                                    if (attr != null && !"".equals(getText(attr))) {
                                                        project.TomitaFactField = getText(attr);
                                                    }
                                                }
                                                break;
                                                
                                            case "blocks":
                                                NodeList blocks = project_item.getChildNodes();
                                                for (int l = 0; l < blocks.getLength(); l++) { 
                                                    Node block_node = blocks.item(l);
                                                    if (block_node.getNodeType() == Node.ELEMENT_NODE) {
                                                        Block block = new Block(block_node.getNodeName());
                                                        project.blocks.add(block);

                                                        NodeList block_conf = block_node.getChildNodes();
                                                        for (int n=0; n < block_conf.getLength(); n++) {
                                                            Node block_item = block_conf.item(n);
                                                            if (block_item.getNodeType() == Node.ELEMENT_NODE) {
                                                                switch (block_item.getNodeName()) {

                                                                    case "assignees":
                                                                        NodeList assignees = block_item.getChildNodes();
                                                                        for (int m = 0; m < assignees.getLength(); m++) { 
                                                                            Node assignee = assignees.item(m);
                                                                            if (assignee.getNodeType() == Node.ELEMENT_NODE
                                                                                && "assignee".equals(assignee.getNodeName())
                                                                                && !"".equals(getText(assignee))) {
                                                                                    block.Assignees.add(getText(assignee));
                                                                            }
                                                                        }
                                                                        break;

                                                                    case "keywords":
                                                                        NodeList keywords = block_item.getChildNodes();
                                                                        for (int m = 0; m < keywords.getLength(); m++) {
                                                                            Node keyword = keywords.item(m);
                                                                            if (keyword.getNodeType() == Node.ELEMENT_NODE
                                                                                && "keyword".equals(keyword.getNodeName())
                                                                                && !"".equals(getText(keyword))) {
                                                                                    block.Keys.put(getText(keyword).toLowerCase()
                                                                                                 , getAttrInt(keyword, "weight", 1));
                                                                            }
                                                                        }
                                                                        break;

                                                                    case "reporters":
                                                                        NodeList reporters = block_item.getChildNodes();
                                                                        for (int m = 0; m < reporters.getLength(); m++) {
                                                                            Node reporter = reporters.item(m);
                                                                            if (reporter.getNodeType() == Node.ELEMENT_NODE
                                                                                && "reporter".equals(reporter.getNodeName())
                                                                                && !"".equals(getText(reporter))) {
                                                                                    block.Reporters.put(getText(reporter)
                                                                                                      , getAttrInt(reporter, "weight", 1));
                                                                            }
                                                                        }
                                                                        break;

                                                                    case "labels":
                                                                        NodeList labels = block_item.getChildNodes();
                                                                        for (int m = 0; m < labels.getLength(); m++) {
                                                                            Node label = labels.item(m);
                                                                            if (label.getNodeType() == Node.ELEMENT_NODE
                                                                                && "label".equals(label.getNodeName())
                                                                                && !"".equals(getText(label))) {
                                                                                    block.Labels.put(getText(label)
                                                                                                   , getAttrInt(label, "weight", 1));
                                                                            }
                                                                        }
                                                                        break;

                                                                    case "edit_fields":
                                                                        edit_fields = block_item.getChildNodes();
                                                                        for (int m = 0; m < edit_fields.getLength(); m++) {
                                                                            Node field = edit_fields.item(m);
                                                                            if (field.getNodeType() == Node.ELEMENT_NODE
                                                                                && !"".equals( getText(field))) {
                                                                                    @SuppressWarnings("unchecked")
                                                                                    Pair<String, String> edits = new Pair(field.getNodeName(), getText(field));
                                                                                    block.EditFields.add(edits);
                                                                            }
                                                                        }
                                                                        break;
                                                                    default:
                                                                        Logging.PrintWarn("Unknown tag " + block_item.getNodeName() + " in block " + block.BlockName);
                                                                        break;
                                                                } // end switch blocks
                                                            }
                                                        }
                                                    }
                                                }
                                                break; // blocks
                                            default:
                                                Logging.PrintWarn("Unknown tag " + project_item.getNodeName() + " in project " + project.Name);
                                                break;
                                        } // end switch projects
                                    }
                                }
                            }
                        }
                        break; // projects
                    default:
                        Logging.PrintWarn("Unknown tag " + config_node.getNodeName());
                        break;
                } // end switch config
            }
        }
    
    return Projects;
    }
  
}