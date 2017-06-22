package ubb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Patterns {
	
	CHAR("CHAR"),
	NUMBER("NUMBER"),
	ATTRIBUTES("\\([a-z0-9 A-Z_,]+\\)"),
	TABLE_ATTRIBUTES("\\([a-z 0-9A-Z_\\.,\\(\\)]+\\)"),
	PRIMARY_KEY("PRIMARY KEY"),
	NOT_NULL("NOT NULL"),
	UNIQUE("UNIQUE"),
	FOREIGN_KEY("FOREIGN KEY"),
	JOIN("JOIN [A-Z_a-z0-9]+ [A-Za-z0-9]+ ON [A-Za-z0-9]+\\.[A-Z_a-z0-9]+=[A-Za-z0-9]+\\.[A-Z_a-z0-9]+");
	
	private String pattern;
	private Patterns(String pattern){
		this.pattern = pattern;
	}
	
	public Pattern getPattern(){
		return Pattern.compile(pattern);
	}
	
	public Matcher getMatcher(String input){
		return getPattern().matcher(input);
	}

}
