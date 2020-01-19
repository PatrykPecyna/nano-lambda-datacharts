package nano;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;
import org.apache.commons.lang.StringUtils;

import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Vector;

public class Main implements RequestHandler<RequestClass, ResponseClass> {

    public ResponseClass handleRequest(RequestClass request, Context context) {
        ResponseClass response = null;
        try {
            response = go(Integer.parseInt(request.getTop()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public ResponseClass go(int topValue) throws Exception {
        String sshHost = "ssh.pythonanywhere.com";
        String sshUser = "nanoindexer";
        String sshPassword = "inzynieriaoprogramowania";
        String database = "nanoindexer$nano";
        String dbUser = "nanoindexer";
        String dbPassword = "inzynieria";
        int sshPort = 22;
        int tunnelLocalPort = 9080;
        String tunnelRemoteHost = "nanoindexer.mysql.pythonanywhere-services.com";
        int tunnelRemotePort = 3306;
        String driver = "com.mysql.cj.jdbc.Driver";
        String connectionUrl = "jdbc:mysql://localhost:" + tunnelLocalPort + "/";

        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");

        JSch jsch= new JSch();
        Session session = jsch.getSession(sshUser,sshHost,sshPort);
        localUserInfo lui = new localUserInfo();
        session.setPassword(sshPassword);
        session.setConfig(config);
        session.setUserInfo(lui);
        session.connect();
        session.setPortForwardingL(tunnelLocalPort,tunnelRemoteHost,tunnelRemotePort);

        Class.forName(driver);
        Connection connection = DriverManager.getConnection(connectionUrl + database, dbUser, dbPassword);
        String query;

        ResponseClass responseObject = new ResponseClass();

        Vector<String> queries = new Vector<String>();

        //active_accounts_last_24h
        queries.add("SELECT account,timestamp FROM nanosite_tabela WHERE timestamp > DATE_SUB(CURDATE(), INTERVAL 1 DAY)");
        responseObject.setActive_accounts_last_24h(accountsAmountsTimestampsResponse(queries.get(0), connection, "account"));

        //accounts_with_income_last_24h
        queries.add("SELECT account,timestamp FROM nanosite_tabela WHERE timestamp > DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND type = 'receive'");
        responseObject.setAccounts_with_income_last_24h(accountsAmountsTimestampsResponse(queries.get(1), connection, "account"));

        //accounts_with_outgo_last_24h
        queries.add("SELECT account,timestamp FROM nanosite_tabela WHERE timestamp > DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND type = 'send'");
        responseObject.setAccounts_with_outgo_last_24h(accountsAmountsTimestampsResponse(queries.get(2),connection, "account"));

        //amount_sent_last_24h
        queries.add("SELECT amount,timestamp FROM nanosite_tabela WHERE timestamp > DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND type = 'send'");
        responseObject.setAmount_sent_last_24h(accountsAmountsTimestampsResponse(queries.get(3),connection, "amount"));

        //amount_received_last_24h
        queries.add("SELECT amount,timestamp FROM nanosite_tabela WHERE timestamp > DATE_SUB(CURDATE(), INTERVAL 1 DAY) AND type = 'receive'");
        responseObject.setAmount_received_last_24h(accountsAmountsTimestampsResponse(queries.get(4),connection, "amount"));

        //accounts_with_income_top10
        queries.add("SELECT account, SUM(amount) AS amount FROM nanosite_tabela WHERE type = 'send' GROUP BY account ORDER BY amount DESC LIMIT " + topValue);
        responseObject.setAccounts_with_income_top10(accountsAmountResponse(queries.get(5),connection));

        //accounts_with_outgo_top10
        queries.add("SELECT account, SUM(amount) AS amount FROM nanosite_tabela WHERE type = 'receive' GROUP BY account ORDER BY amount DESC LIMIT " + topValue);
        responseObject.setAccounts_with_outgo_top10(accountsAmountResponse(queries.get(6),connection));

        connection.close();
        session.disconnect();

        return responseObject;
    }

    public LinkedHashMap<String, String> accountsAmountsTimestampsResponse(String query, Connection connection, String action) throws SQLException {
        LinkedHashMap<String, String> mapResults = new LinkedHashMap<String, String>();
        DateTimeFormatter mainFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-");
        DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("HH");
        for (int i=23;i>=0;i--) {
            LocalDateTime moment = LocalDateTime.now(ZoneId.of("UCT")).minusHours(i);
            mapResults.put(moment.format(mainFormatter) + moment.plusHours(1).format(hourFormatter), "0");
        }
        Vector<DataBlock> data = getOutput(query, connection);
        DateTimeFormatter loopFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH");
        System.out.println("Data size: " + data.size());
        if (action.equals("account")) {
            for (int i=0;i<data.size();i++) {
                LocalDateTime caughtDate = LocalDateTime.parse(data.get(i).getTimestamp().substring(0,13),loopFormatter);
                if (mapResults.get(caughtDate.format(mainFormatter) + caughtDate.plusHours(1).format(hourFormatter)) != null) {
                    Integer currentValue = Integer.parseInt(mapResults.get(caughtDate.format(mainFormatter) + caughtDate.plusHours(1).format(hourFormatter)));
                    currentValue++;
                    mapResults.replace(caughtDate.format(mainFormatter) + caughtDate.plusHours(1).format(hourFormatter), currentValue.toString());
                }
            }
        } else if (action.equals("amount")) {
            for (int i=0;i<data.size();i++) {
                LocalDateTime caughtDate = LocalDateTime.parse(data.get(i).getTimestamp().substring(0,13),loopFormatter);
                if (mapResults.get(caughtDate.format(mainFormatter) + caughtDate.plusHours(1).format(hourFormatter)) != null) {
                    BigInteger currentValue = new BigInteger(mapResults.get(caughtDate.format(mainFormatter) + caughtDate.plusHours(1).format(hourFormatter)));
                    BigInteger additionalData = new BigInteger(data.get(i).getAmount());
                    BigInteger newValue = currentValue.add(additionalData);
                    mapResults.replace(caughtDate.format(mainFormatter) + caughtDate.plusHours(1).format(hourFormatter), newValue.toString(10));
                }
            }
        }
        return mapResults;
    }

    public LinkedHashMap<String, String> accountsAmountResponse(String query, Connection connection) throws SQLException {
        LinkedHashMap<String, String> mapResults = new LinkedHashMap<String, String>();
        Vector<DataBlock> data = getOutput(query, connection);
        for (int i=0;i<data.size();i++) {
            mapResults.put(data.get(i).getAccount(), data.get(i).getAmount());
            //System.out.println("Map element: " +data.get(i).getAccount() + " " + data.get(i).getAmount());
        }
        //System.out.println("AccountsAmountResponse: generated " + data.size() + " blocks of data");
        return mapResults;
    }

    public Vector<DataBlock> getOutput (String query, Connection connection) throws SQLException {
        Statement statement = connection.createStatement();

        Vector<DataBlock> output = new Vector<DataBlock>();
        String queryFieldsPart = null;
        queryFieldsPart = StringUtils.substringBetween(query,"SELECT "," FROM");
        Vector<String> fields = new Vector<String>();
        System.out.println("Done with fetching. Query fields part: " + queryFieldsPart);

        while (true) {
            if (queryFieldsPart.contains(",")) {
                String queryFieldSubstring = StringUtils.substringBefore(queryFieldsPart,",");
                if (queryFieldSubstring.contains("SUM") && queryFieldSubstring.contains("AS")) {
                    fields.add(StringUtils.substringAfter(queryFieldSubstring,"AS "));
                } else {
                    fields.add(queryFieldSubstring);
                }
                queryFieldsPart = queryFieldsPart.substring(fields.lastElement().length()+1);
            } else {
                if (queryFieldsPart.contains("SUM") && queryFieldsPart.contains("AS")) {
                    fields.add(StringUtils.substringAfter(queryFieldsPart,"AS "));
                } else {
                    fields.add(queryFieldsPart);
                }
                break;
            }
        }

        System.out.println("Fields: ");
        for (int i=0;i<fields.size();i++) {
            System.out.print("[" + i + "]" + fields.get(i) + " ");
        }
        System.out.print("\n");

        ResultSet resultSet = statement.executeQuery(query);
        ResultSetMetaData resultSetMD = resultSet.getMetaData();

        while (resultSet.next()) {
            if (queryFieldsPart.equals("*")) {
                output.add(new DataBlock(Integer.parseInt(resultSet.getString(1)),
                        resultSet.getString(2),resultSet.getString(3),
                        resultSet.getString(4),resultSet.getString(5),
                        resultSet.getString(6),resultSet.getString(7),
                        resultSet.getString(8)));
            } else {
                if (resultSetMD.getColumnCount() < fields.size()) {
                    System.out.println("Record: " + resultSet.getString(1) + " contains to few values to put into Data Block object. Expected: " + fields.size() + ". Actual: " + resultSetMD.getColumnCount() + ".");
                }
                else {
                    output.add(new DataBlock());
                    //System.out.println("Created an empty DataBlock");
                    for (int i=0;i<fields.size();i++) {
                        if (resultSet.getString(i+1) == null) {
                            output.lastElement().setValue(fields.get(i),"null");
                        } else {
                            if (fields.get(i).equals("id")) {
                                //System.out.println("Passing data to setValue: " + fields.get(i) + ", " + Integer.parseInt(resultSet.getString(i+1)) + ".");
                                output.lastElement().setValue(fields.get(i),Integer.parseInt(resultSet.getString(i+1)));
                            } else {
                                //System.out.println("Passing data to setValue: " + fields.get(i) + ", " + resultSet.getString(i+1) + ".");
                                output.lastElement().setValue(fields.get(i),resultSet.getString(i+1));
                            }
                        }
                    }
                }
            }
        }

        statement.close();
        System.out.println("Generated output contains: " + output.size() + " elements.");
        return output;
    }

    class localUserInfo implements UserInfo {
        String passwd;
        public String getPassphrase() {
            return null;
        }
        public String getPassword() {
            return null;
        }
        public boolean promptPassword(String s) {
            return false;
        }
        public boolean promptPassphrase(String s) {
            return false;
        }
        public boolean promptYesNo(String s) {
            return false;
        }
        public void showMessage(String s) {
        }
    }
}
