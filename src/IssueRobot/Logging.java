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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Logging {
    final static String crlf = System.getProperty("line.separator");
    final static String LogPath = "RoutingLog.csv";
    final static String delim = "|";
    static boolean SilentOff = true;

    public static void log(String str) {
        if (SilentOff)
            System.out.println(str);
    }

    public static void ClearLine() {
        if (SilentOff)
            System.out.printf("\r%-80s", "");
    }
    
    public static void PrintProgress(String log) {
        if (SilentOff)
            System.out.printf("\r%1$-80s", log);
    }
    
    public static void PrintErr(String log) {
        System.out.printf("\rError! %1$-80s%n", log);
    }

    public static void PrintWarn(String log) {
        System.out.printf("\rWarning! %1$-80s%n", log);
    }
    
    public static void logIssueDetails(String name,
                                        String key,
                                        String old_assignee,
                                        String reporter,
                                        String block_name,
                                        String new_assignee,
                                        String blocks_points,
                                        String summary,
                                        String labels,
                                        String facts,
                                        String comment_facts) {

        File file = new File(LogPath);
        StringBuilder sb = new StringBuilder(200);
        try{
            if(!file.exists()){
                file.createNewFile();
                
                // add file header if file is new
                sb.append("project").append(delim);
                sb.append("key").append(delim);
                sb.append("old assignee").append(delim);
                sb.append("reporter").append(delim);
                sb.append("block").append(delim);
                sb.append("new assignee").append(delim);
                sb.append("blocks points").append(delim);
                sb.append("summary").append(delim);
                sb.append("labels").append(delim);
                sb.append("issue facts").append(delim);
                sb.append("comment facts").append(delim);
                sb.append(crlf);
            }
            
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LogPath, true))) {
            sb.append(name).append(delim);
            sb.append(key).append(delim);
            sb.append(old_assignee).append(delim);
            sb.append(reporter).append(delim);
            sb.append(block_name).append(delim);
            sb.append(new_assignee).append(delim);
            sb.append(blocks_points).append(delim);
            sb.append(summary.replace(delim, " ")).append(delim);
            sb.append(labels).append(delim);
            sb.append(facts).append(delim);
            sb.append(comment_facts).append(delim);
            sb.append(crlf);
            writer.write(sb.toString());
        }
        } catch(IOException e) {
            PrintErr(e.getMessage());
        }
    }
}
