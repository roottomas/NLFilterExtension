package modules;

public class ClarificationExchange {
    private String question;
    private String answer;
    private String topic;

    public ClarificationExchange() {}

    public ClarificationExchange(String question, String answer) {
        this(question, answer, null);
    }

    public ClarificationExchange(String question, String answer, String topic) {
        this.question = question;
        this.answer = answer;
        this.topic = topic;
    }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
}