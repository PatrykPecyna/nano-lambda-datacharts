package nano;

import java.util.Map;

public class ResponseClass {
    private Map<String,String> active_accounts_last_24h;
    private Map<String,String> accounts_with_income_last_24h;
    private Map<String,String> accounts_with_outgo_last_24h;
    private String amount_sent_last_24h;
    private String amount_received_last_24h;
    private Map<String,String> accounts_with_income_top10;
    private Map<String,String> accounts_with_outgo_top10;

    ResponseClass() { }

    public Map<String, String> getActive_accounts_last_24h() {
        return active_accounts_last_24h;
    }

    public void setActive_accounts_last_24h(Map<String, String> active_accounts_last_24h) {
        this.active_accounts_last_24h = active_accounts_last_24h;
    }

    public Map<String, String> getAccounts_with_income_last_24h() {
        return accounts_with_income_last_24h;
    }

    public void setAccounts_with_income_last_24h(Map<String, String> accounts_with_income_last_24h) {
        this.accounts_with_income_last_24h = accounts_with_income_last_24h;
    }

    public Map<String, String> getAccounts_with_outgo_last_24h() {
        return accounts_with_outgo_last_24h;
    }

    public void setAccounts_with_outgo_last_24h(Map<String, String> accounts_with_outgo_last_24h) {
        this.accounts_with_outgo_last_24h = accounts_with_outgo_last_24h;
    }

    public String getAmount_sent_last_24h() {
        return amount_sent_last_24h;
    }

    public void setAmount_sent_last_24h(String amount_sent_last_24h) {
        this.amount_sent_last_24h = amount_sent_last_24h;
    }

    public String getAmount_received_last_24h() {
        return amount_received_last_24h;
    }

    public void setAmount_received_last_24h(String amount_received_last_24h) {
        this.amount_received_last_24h = amount_received_last_24h;
    }

    public Map<String, String> getAccounts_with_income_top10() {
        return accounts_with_income_top10;
    }

    public void setAccounts_with_income_top10(Map<String, String> accounts_with_income_top10) {
        this.accounts_with_income_top10 = accounts_with_income_top10;
    }

    public Map<String, String> getAccounts_with_outgo_top10() {
        return accounts_with_outgo_top10;
    }

    public void setAccounts_with_outgo_top10(Map<String, String> accounts_with_outgo_top10) {
        this.accounts_with_outgo_top10 = accounts_with_outgo_top10;
    }
}
