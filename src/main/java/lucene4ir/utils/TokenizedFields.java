package lucene4ir.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.HashMap;
import java.util.List;


public class TokenizedFields {
public List<Field>  fields = null;
    public TokenizedFields (String fieldsListFile)
    {
        if (!fieldsListFile.isEmpty())
            fields = JAXB.unmarshal(new File(fieldsListFile), Fields.class).fields;
    } // End Function

    public Analyzer getWrappedAnalyzer (Analyzer defaultAnalyzer)
    {
        String fldName , tokenFilterFile;
        HashMap<String , Analyzer> analyzerList = new HashMap<String, Analyzer>();
        TokenAnalyzerMaker tam = new TokenAnalyzerMaker();
        PerFieldAnalyzerWrapper wrapper;

        for (int i = 0 ; i < fields.size() ; i++)
        {
            fldName = getFieldName(i);
            tokenFilterFile = getFieldTokenFilterFile(i);
            analyzerList.put(fldName,tam.createAnalyzer(tokenFilterFile));
        } // End For
        wrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer , analyzerList);
        return wrapper;
    } // End Function

    public String getFieldName (int fldNumber)
    {
        return this.fields.get(fldNumber).fieldName;
    }

    public String getFieldTokenFilterFile (int fldNumber)
    {
        return this.fields.get(fldNumber).tokenFilterFile;
    }

    public int getSize()
    {
        return fields.size();
    }

} // End Class

@XmlRootElement(name = "fields")
class Fields {
    @XmlElement(name = "field")
    public List<Field> fields;
    @XmlElement(name = "retrievalParamsFile")
    public String retrievalParamsFile;
}

@XmlRootElement(name = "field")
@XmlAccessorType(XmlAccessType.FIELD)

class Field {
    @XmlElement(name = "fieldName")
    public String fieldName;
    @XmlElement(name = "tokenFilterFile")
    public String tokenFilterFile;
    @XmlElement(name = "fieldBoost")
    public float fieldBoost;
}


