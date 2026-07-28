package modules;

import java.util.List;

public class NLQueryFunctionInput {

    private Long scenario;
    private Long version;
    private String query;
    private List<ClarificationExchange> clarificationHistory;

    public NLQueryFunctionInput() { }

    public NLQueryFunctionInput(Long scenario, Long version, String query) {
        this.scenario = scenario;
        this.version = version;
        this.query = query;
    }

    public NLQueryFunctionInput(Long scenario, Long version, String query,
                                List<ClarificationExchange> clarificationHistory) {
        this.scenario = scenario;
        this.version = version;
        this.query = query;
        this.clarificationHistory = clarificationHistory;
    }

    public Long getScenario() {
        return scenario;
    }

    public void setScenario(Long scenario) {
        this.scenario = scenario;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<ClarificationExchange> getClarificationHistory() {
        return clarificationHistory;
    }

    public void setClarificationHistory(List<ClarificationExchange> clarificationHistory) {
        this.clarificationHistory = clarificationHistory;
    }
}