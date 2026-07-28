package modules;

public class ClarificationOption {
    private String id;
    private String label;
    private String value;

    public ClarificationOption() {}

    public ClarificationOption(String id, String label, String value) {
        this.id = id;
        this.label = label;
        this.value = value;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}