package IssueRobot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class BlockTest {
    Block block;
    
    @Before
    public void setUp() {
        block = new Block("test");
        block.Assignees.add("Assignee1");
        block.Assignees.add("Assignee2");
        block.Assignees.add("Assignee3");
    }
    
    @After
    public void tearDown() {
        block = null;
    }

    /**
     * Test of switch_executor method, of class Block.
     */
    @Test
    public void testSwitch_executor() {
        System.out.println("switch_executor");
        String expResult = "Assignee2";
        // 123 => 213 and return first element
        String result = block.switch_executor();
        assertEquals(expResult, result);
        // 213 => 123 and return first element
        expResult = "Assignee1";
        result = block.switch_executor();
        assertEquals(expResult, result);

        block.BlockQueue += 1;
        // 123 => 132 and return second element
        expResult = "Assignee3";
        result = block.switch_executor();
        assertEquals(expResult, result);
        
        expResult = "Assignee2";
        result = block.switch_executor();
        assertEquals(expResult, result);
        
        block.BlockQueue += 1;
        expResult = "Assignee1";
        result = block.switch_executor();
        assertEquals(expResult, result);
        
        expResult = "Assignee3";
        result = block.switch_executor();
        assertEquals(expResult, result);
        
        block.Assignees = new ArrayList<String>();
        result = block.switch_executor();
        assertNull(result);
    }

    /**
     * Test of getNextAssignee method, of class Block.
     */
    @Test
    public void testGetNextAssignee() {
        System.out.println("getNextAssignee");
        String expResult = "Assignee1";
        String result = block.getNextAssignee();
        assertEquals(expResult, result);
        
        expResult = "Assignee2";
        result = block.getNextAssignee();
        assertEquals(expResult, result);
        
        expResult = "Assignee3";
        result = block.getNextAssignee();
        assertEquals(expResult, result);
        
        expResult = "Assignee1";
        result = block.getNextAssignee();
        assertEquals(expResult, result);
        
        block.Assignees = new ArrayList<String>();
        result = block.switch_executor();
        assertNull(result);
    }

    /**
     * Test of checkTicketKeywords method, of class Block.
     */
    @Test
    public void testCheckTicketKeywords() {
        System.out.println("checkTicketKeywords");
        HashSet<String> facts = null;
        int expResult = 0;
        int result = block.checkTicketKeywords(facts);
        assertEquals(expResult, result);
        
        block.Keys.put("factA", 1);
        block.Keys.put("factB", 5);
        block.Keys.put("factC", -1);
        facts = new HashSet<String>();
        facts.add("fact1"); // false
        expResult = 0;
        result = block.checkTicketKeywords(facts);
        assertEquals(expResult, result);
        
        facts.add("factA");
        expResult = 1;
        result = block.checkTicketKeywords(facts);
        assertEquals(expResult, result);
        
        facts.add("factB");
        expResult = 6;
        result = block.checkTicketKeywords(facts);
        assertEquals(expResult, result);
        
        facts.add("factC");
        expResult = 5;
        result = block.checkTicketKeywords(facts);
        assertEquals(expResult, result);
        
        block.Keys.clear();
        expResult = 0;
        result = block.checkTicketKeywords(facts);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkTicketLabels method, of class Block.
     */
    @Test
    public void testCheckTicketLabels() {
        System.out.println("checkTicketLabels");
        List<String> labels = null;
        int expResult = 0;
        int result = block.checkTicketLabels(labels);
        assertEquals(expResult, result);
        
        block.Labels.put("label", 3);
        block.Labels.put("badlabel", -4);
        labels = new ArrayList<String>();
        labels.add("Label"); // false
        expResult = 0;
        result = block.checkTicketLabels(labels);
        assertEquals(expResult, result);
        
        labels.add("label");
        expResult = 3;
        result = block.checkTicketLabels(labels);
        assertEquals(expResult, result);
        
        labels.add("badlabel");
        expResult = -1;
        result = block.checkTicketLabels(labels);
        assertEquals(expResult, result);
    }

    /**
     * Test of checkTicketReporter method, of class Block.
     */
    @Test
    public void testCheckTicketReporter() {
        System.out.println("checkTicketReporter");
        String reporter = null;
        int expResult = 0;
        int result = block.checkTicketReporter(reporter);
        assertEquals(expResult, result);
        
        reporter = "";
        expResult = 0;
        result = block.checkTicketReporter(reporter);
        assertEquals(expResult, result);
        
        reporter = "reporter1";
        expResult = 0;
        result = block.checkTicketReporter(reporter);
        assertEquals(expResult, result);
        
        block.Reporters.put("reporter1", 5);
        expResult = 5;
        result = block.checkTicketReporter(reporter);
        assertEquals(expResult, result);
        
        reporter = "reporter2";
        block.Reporters.put("reporter2", 10);
        expResult = 10;
        result = block.checkTicketReporter(reporter);
        assertEquals(expResult, result);
        
        reporter = "reporter3";
        block.Reporters.put("reporter3", -3);
        expResult = -3;
        result = block.checkTicketReporter(reporter);
        assertEquals(expResult, result);
    }
    
}
