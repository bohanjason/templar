package edu.umich.templar.rdbms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class MappedSchemaElement implements Comparable<MappedSchemaElement>, Serializable
{
	public SchemaElement schemaElement; 
	public double similarity = -1;

    // Added by cjbaik
    public String attachedFT = null; // the attached FT function

	public List<String> mappedValues = new ArrayList<String>();
	public int choice; 
	
	public MappedSchemaElement(SchemaElement schemaElement)
	{
		this.schemaElement = schemaElement; 
	}

	public int compareTo(MappedSchemaElement element) 
	{
		if(this.similarity > element.similarity)
		{
			return -1; 
		}
		else if(element.similarity > this.similarity)
		{
			return 1; 
		}
		return 0;
	}

	// Is a valid candidate to generate a "having" query fragment
	public boolean isValidHavingCandidate() {
		// Needs to have an attached function to be a "having"
		if (this.attachedFT == null) return false;

		boolean isNumberAndCount = this.schemaElement.isNumeric() && this.attachedFT.equals("count");
		boolean isTextAndNotCount = this.schemaElement.isText() && !this.attachedFT.equals("count");

		return !isNumberAndCount && !isTextAndNotCount;
	}

	public String printForCheck() 
	{
		String result = "";

		if (schemaElement.type.equals("relation")) {
            result += schemaElement.relation.name + "(" + (double)Math.round(this.similarity*100)/100 + ")" + ":";
        } else {
            result += schemaElement.relation.name + "." + schemaElement.name + "(" + (double) Math.round(this.similarity * 100) / 100 + ")" + ":";
        }

		if(mappedValues.size() > 0 && choice >= 0)
		{
			for(int i = 0; i < mappedValues.size() && i < 3; i++)
			{
				String value = mappedValues.get(i); 
				if(value.length() > 20)
				{
					value = value.substring(0, 20); 
				}
				result += value + "; "; 
			}
		}
		return result;
	}
}
