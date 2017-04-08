package ubb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Patterns {
	
	CHAR("CHAR"),
	NUMBER("NUMBER"),
	ATTRIBUTES("\\(.*\\)"),
	PRIMARY_KEY("PRIMARY KEY"),
	NOT_NULL("NOT NULL"),
	UNIQUE("UNIQUE");
	
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
