package ubb.model;

public class ForeignKey {
	
	private String attName;
	private String refTable;
	private String refAttr;
	
	public String getAttName() {
		return attName;
	}
	public void setAttName(String attName) {
		this.attName = attName;
	}
	public String getRefTable() {
		return refTable;
	}
	public void setRefTable(String refTable) {
		this.refTable = refTable;
	}
	public String getRefAttr() {
		return refAttr;
	}
	public void setRefAttr(String refAttr) {
		this.refAttr = refAttr;
	}

}
