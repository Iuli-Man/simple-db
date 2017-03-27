package ubb.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum Patterns {
	
	PK("PK"),
	CHAR("char"),
	INTEGER("integer"),
	ATTRIBUTES("\\(.*\\)"),
	PRIMARY_KEY("primary key"),
	NOT_NULL("not null"),
	UNIQUE("unique");
	
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
