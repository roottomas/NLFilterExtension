package modules;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class NLQueryFunctionOutput {

    private boolean success;
    private String message;
    private String clarificationQuestion;
    private String clarificationType;
    private List<ClarificationOption> clarificationOptions;
    private String clarificationTopic;
    private String field;
    private String unit;

    public static NLQueryFunctionOutput success(String filterMessage) {
        NLQueryFunctionOutput output = new NLQueryFunctionOutput();
        output.setSuccess(true);
        output.setMessage(filterMessage);
        return output;
    }

    public static NLQueryFunctionOutput error(String errorMessage) {
        NLQueryFunctionOutput output = new NLQueryFunctionOutput();
        output.setSuccess(false);
        output.setMessage(errorMessage);
        return output;
    }

    public static NLQueryFunctionOutput multiChoice(String question, List<ClarificationOption> options) {
        return multiChoice(question, options, null);
    }

    public static NLQueryFunctionOutput multiChoice(String question, List<ClarificationOption> options, String topic) {
        NLQueryFunctionOutput output = new NLQueryFunctionOutput();
        output.setSuccess(false);
        output.setClarificationQuestion(question);
        output.setClarificationType("multi_choice");
        output.setClarificationOptions(options);
        output.setClarificationTopic(topic != null ? topic : "GENERIC_CHOICE");
        return output;
    }

    public static NLQueryFunctionOutput numericThreshold(String question, String field, String unit) {
        return numericThreshold(question, field, unit, null);
    }

    public static NLQueryFunctionOutput numericThreshold(String question, String field, String unit, String topic) {
        NLQueryFunctionOutput output = new NLQueryFunctionOutput();
        output.setSuccess(false);
        output.setClarificationQuestion(question);
        output.setClarificationType("numeric_threshold");
        output.setField(field);
        output.setUnit(unit);
        output.setClarificationTopic(topic != null ? topic : "GENERIC_NUMERIC");
        return output;
    }

    public static NLQueryFunctionOutput freeText(String question) {
        return freeText(question, null);
    }

    public static NLQueryFunctionOutput freeText(String question, String topic) {
        NLQueryFunctionOutput output = new NLQueryFunctionOutput();
        output.setSuccess(false);
        output.setClarificationQuestion(question);
        output.setClarificationType("free_text");
        output.setClarificationTopic(topic != null ? topic : "GENERIC_TEXT");
        return output;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getClarificationQuestion() { return clarificationQuestion; }
    public void setClarificationQuestion(String clarificationQuestion) { this.clarificationQuestion = clarificationQuestion; }

    public String getClarificationType() { return clarificationType; }
    public void setClarificationType(String clarificationType) { this.clarificationType = clarificationType; }

    public List<ClarificationOption> getClarificationOptions() { return clarificationOptions; }
    public void setClarificationOptions(List<ClarificationOption> clarificationOptions) { this.clarificationOptions = clarificationOptions; }

    public String getClarificationTopic() { return clarificationTopic; }
    public void setClarificationTopic(String clarificationTopic) { this.clarificationTopic = clarificationTopic; }

    public String getField() { return field; }
    public void setField(String field) { this.field = field; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}