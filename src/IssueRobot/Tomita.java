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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Tomita {
    
    /*
        Чтобы проблем с кодировками не было, нужно в конфиге парсера (Consts.TOMITA_CONFIG) указать кириллицу на входе и выходе:
        Input = { Encoding = "Cp1251"; }
        Output = { Encoding = "Cp1251"; }
    */
    String TomitaPath = System.getProperty("user.dir") + "\\parser";
    String TOMITA_CONFIG;
    String FACT_FIELD;
    
    private final File tomitaBin = new File(TomitaPath + "\\tomitaparser.exe");
    private final File workDir =  new File(TomitaPath);
       
    public Tomita(String config, String factField) {
        TOMITA_CONFIG = config;
        FACT_FIELD = factField;
    }
    
    private class TomitaProcess {
        private final Process tomitaProcess;
        
        TomitaProcess() throws IOException {
            ProcessBuilder processBuilder = new ProcessBuilder(tomitaBin.toString(), TOMITA_CONFIG).directory(workDir);
            tomitaProcess = processBuilder.start();
        }

        String stdoutRead() throws IOException {
            BufferedReader reader = new BufferedReader(new InputStreamReader(tomitaProcess.getInputStream()));
            StringBuilder builder = new StringBuilder(200);
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append(Logging.crlf);
            }
            return builder.toString();
        }

        void stdinWrite(String target) throws IOException {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(tomitaProcess.getOutputStream()))) {
                writer.write(target);
                writer.flush();
            }
        }

        int waitFor() throws InterruptedException {
            return tomitaProcess.waitFor();
        }
    }
    
    /**
     * Converts string to XML document
     * @param {String} xml
     * @return Document
     * @throws Exception 
     */
    private Document getDocument(String xml) throws Exception {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setValidating(false);
            DocumentBuilder builder = f.newDocumentBuilder();
            return builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(xml)));
        } catch (ParserConfigurationException | SAXException | IOException exception) {
            throw new Exception("Tomita XML parsing error!" + exception.toString());
        }
    }
 
    /**
     * parse text with Tomita parser
     * @param {String} inputString
     * @return 
     * @throws IOException
     * @throws InterruptedException 
     */
    public Document parse(String inputString) throws IOException, InterruptedException, Exception {
        TomitaProcess tomita = new TomitaProcess();
        tomita.stdinWrite(inputString);
        String result = tomita.stdoutRead();
        tomita.waitFor();
        return getDocument(result);
    }

     /**
     * Returns facts from String
     * @param {String} TextToParse
     * @throws Exception 
     */
    public HashSet<String> parseFactsFromText(String TextToParse) throws Exception {
        HashSet<String> facts = new HashSet<String>();
        if (TextToParse != null && TextToParse.length() > 1 ) {

            Document doc = parse(TextToParse);
            doc.getDocumentElement().normalize();
            NodeList fact_list = doc.getElementsByTagName(FACT_FIELD);

            for (int i = 0; i < fact_list.getLength(); i++) {
                facts.add(fact_list.item(i)
                          .getAttributes()
                          .getNamedItem("val")
                          .getNodeValue()
                          .toLowerCase());
            } 
        }
        return facts;
    }
}
