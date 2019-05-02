package IssueRobot;

import javafx.util.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ProjectTest {
    
    Project project;
    
    @Before
    public void setUp() {
        project = new Project("test");
    }
    
    @After
    public void tearDown() {
        project = null;
    }

    /**
     * Test of isValid method, of class Project.
     */
    @Test
    public void testIsValid1() {
        System.out.println("isValid1");
        boolean expResult = false;
        boolean result = project.isValid();
        assertEquals(expResult, result);
        
        System.out.println("add jql and empty block");
        project.jql = "status=Open";
        project.blocks.add(new Block("testBlock"));
        result = project.isValid();
        assertEquals(expResult, result);
        assertEquals(0, project.blocks.size()); // block removed
        result = project.isValid();
        assertEquals(expResult, result); // only jql filled and still not valid
    }
    
    /**
     * Test of isValid method, of class Project.
     */
    @Test
    public void testIsValid2() {
        System.out.println("isValid2");
        project.jql = "status=Open";
        boolean expResult = true;
        
        System.out.println("add assignee in block");
        project.blocks.add(new Block("testBlock"));
        project.blocks.get(0).Assignees.add("SomeAssignee");
        boolean result = project.isValid();
        assertEquals(expResult, result);
        
        System.out.println("add comment");
        project.add_comment = "bla bla";
        result = project.isValid();
        assertEquals(expResult, result);
        
        System.out.println("remove block");
        project.blocks.clear();
        result = project.isValid();
        assertEquals(expResult, result);
    }
    @Test
    public void testIsValidIfLableExists() {
        System.out.println("testIsValidIfLableExists");
        project.jql = "status=Open";
        boolean expResult = true;
        
        project.add_labels.add("label");
        boolean result = project.isValid();
        assertEquals(expResult, result);
    }
    
    @Test
    public void testIsValidIfEditsExists() {
        System.out.println("testIsValidIfEditsExists");
        project.jql = "status=Open";
        boolean expResult = true;
        Pair<String, String> edits = new Pair<String, String>("key", "value");
        project.EditFields.add(edits);
        boolean result = project.isValid();
        assertEquals(expResult, result);
    }
    
}
