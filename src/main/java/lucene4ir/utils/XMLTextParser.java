package lucene4ir.utils;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;

public class XMLTextParser {
    public String originalXML, // The Original XML Text File
                  parsedXML,  // The Parsed XML Text File
                  inFile;
    // Constructor method
    public XMLTextParser(String inXMLFile)
    {
        resetXMLText(inXMLFile);
    } // End Function

    private String readFileText (String fileName)
    {
        // This Function is Used to read an input XML file and return all of its lines gathered in one String variable Excluding Comments

        BufferedReader br; // BufferReader to Read input Parameter File and retrieval Parameter XML File as Text
        String line, // Store the current read line
                result = ""; // Store the result of the function
        try {
            // Read input File
            br = new BufferedReader(new FileReader(fileName));

            while ((line = br.readLine()) != null)
                // Gather XML Lines Execluding comments
                result += line + "\n";

            br.close();
        } // End Try
        catch (Exception e)
        {
            System.out.println("Error Reading XML File " + fileName);
            System.exit(0);
        } // End Catch

        return result;
    }

    private void displayMesg(String msg)
    {
        System.out.println(msg);
        System.exit(0);
    }

    public void resetXMLText(String inXMLFile)
    {
       // This function is Used to reset originalXMLText and ParsedXMLText
        String xmlText;
        // read input File and save it in a String format
        xmlText = readFileText(inXMLFile);
        originalXML = xmlText;
        parsedXML = xmlText;
        inFile = inXMLFile;
    } // End Function

    // Tag Functions
    public String[] splitXML (String XMLText ,String tagName)
    {
        /*
        This Function is Used to Divide Current XML Text File into 3 parts according to given tag Name:
        1- Start : XML Text before tag + open Tag
        2- value : Tag Value
        3- End : close tag + Rest Of XML Text File
         */
        String start ,
                value,
                end;
        String parts[];

        // Identify Opening Tag
        start = String.format("<%s>",tagName);
        // Identify Closing Tag
        end =  String.format("</%s>",tagName);
        // Split Using Opening Tag
        parts = XMLText.split(start,2);
        // Get First Part Of XMLText
        start = parts[0] + start;
        // Split Using End Tag
        parts = parts[1].split(end,2);
        // Get Last Part Of XML Text
        value = parts[0];
        end = end + parts[1];
        return new String[]{start , value , end};
    } // End Function

    public String getTagValue (String tagName)
    {
        // This function is used to get an XML tag content given tag name
        String parts[] , result = "";
        parts = splitXML(parsedXML,tagName);
        if (parts.length > 0)
            result =  parts[1];
        else
            displayMesg(String.format("The tag %s is not available in the following XML File \n %s",tagName, parsedXML));
        return result;
    } // End Function

    public void setTagValue (String tagName , String value)
    {
        /*
        This Function is Used to Set a Value in The given XMLText
        Given Target TagName and TagValue and assign the result in parsed XMLText
         */
        String[] parts = splitXML(parsedXML,tagName);
        // Put The Value Inside Between start and End
        if (parts.length>0)
            parsedXML = parts[0] + value + parts[2];
        else
            displayMesg(String.format("The tag %s is not available in the following XML File \n %s",tagName, parsedXML));
    }; // End Function

    public void renameTag (String oldName , String newName)
    {
        String oldOpenTag,
                oldCloseTag,
                newOpenTag,
                newCloseTag;
        oldOpenTag = String.format("<%s>",oldName);
        oldCloseTag = String.format("</%s>",oldName);
        newOpenTag = String.format("<%s>",newName);
        newCloseTag = String.format("</%s>",newName);
        parsedXML = parsedXML.replaceAll(oldOpenTag,newOpenTag).replaceAll(oldCloseTag,newCloseTag);
    } // End Function

    public void removeTag (String tagName)
    {
        /*
        This Function is Used to remove a Tag From a Given XML Text
         */
        String parts[],
                openTag,
                closeTag;
        openTag = String.format("<%s>",tagName);
        closeTag = String.format("</%s>",tagName);
        parts = splitXML(parsedXML,tagName);
        if (parts.length>0)
            parsedXML =  parts[0].replace(openTag,"") + parts[2].replace(closeTag,"");
        else
            displayMesg(String.format("Error in Removing Tag %s of The Following XML File \n %s",tagName, parsedXML));
    } // End Function

    public void setTagFolder(String tagName , String folderName)
    {
        /*
        This Function is Used to set The Source Folder Of The Tag if it not exist
        Given XMLText , tagName , folderName
            1- get tagValue
            2- check if tagValue contains folderName/
            3- if not add the folder name to the tagValue
        Ex:
        Tag <IndexName> >>> Set "Index/" Folder
        Tag <queryFile> >>> Set Queries/ Folder
         */
        String value , result,
                parts[];

        parts = splitXML(parsedXML , tagName);
        value = parts[1].trim();
        if (!(value.startsWith((folderName+"/"))))
            parsedXML = parts[0] + folderName + "/" + value + parts[2];
    } // Ed Function

    public void save ()
    {
        try {
            PrintWriter pr = new PrintWriter(inFile);
            pr.write(parsedXML);
            pr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main (String args[])
    {
        String inFile;
        int argsCount = args.length;
        if (argsCount > 1 && argsCount % 2 == 1)
        {
            inFile = args[0];
            XMLTextParser parser = new XMLTextParser(inFile);
            for (int i = 1 ; i < argsCount ; i+=2)
                parser.setTagValue(args[i] , args[i+1]);
            try {
                PrintWriter pr = new PrintWriter(inFile);
                pr.write(parser.parsedXML);
                pr.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    // End Tag Functions
} // End Class
