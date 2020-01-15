package nano;

public class RequestClass {
    private String days;
    private String top;

    public RequestClass(String days, String top) {
        this.days = days;
        this.top = top;
    }

    public RequestClass() {
    }

    public String getDays() {
        return days;
    }

    public void setDays(String blockDate) {
        this.days = blockDate;
    }

    public String getTop() {
        return top;
    }

    public void setTop(String top) {
        this.top = top;
    }
}
