package nano;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.sql.*;
import java.util.Vector;

public class Main implements RequestHandler<RequestClass, ResponseClass> {

    public ResponseClass handleRequest(RequestClass request, Context context) {
        String greetingString = null;
        try {
            greetingString = go(request.getBlockId()).getTimestamp();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseClass(greetingString);
    }

    private ResponseClass go(String request) throws Exception {
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

        String driver = "com.mysql.cj.jdbc.Driver";
        String connectionUrl = "jdbc:mysql://localhost:" + tunnelLocalPort + "/";
        Connection connection = null;

        Class.forName(driver);
        connection = DriverManager.getConnection(connectionUrl + database, dbUser, dbPassword);
        Statement statement = connection.createStatement();
        Vector<DataBlock> output = new Vector<DataBlock>();
        String query = "SELECT * FROM nanosite_tabela WHERE block_id='" + request + "'";
        ResultSet resultSet = statement.executeQuery(query);
        ResultSetMetaData resultSetMD = resultSet.getMetaData();

        while (resultSet.next()) {
            System.out.println();
            if (resultSetMD.getColumnCount() < 8) {
                System.out.println("record: " + resultSet.getString(1) + " contains to few values to put into Data Block object");
            }
            else {
                output.add(new DataBlock(Integer.parseInt(resultSet.getString(1)),
                        resultSet.getString(2), resultSet.getString(3),
                        resultSet.getString(4), resultSet.getString(5),
                        resultSet.getString(6), resultSet.getString(7),
                        resultSet.getString(8)));
            }
        }
        connection.close();
        session.disconnect();

        return new ResponseClass(output.lastElement().getTimestamp());
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
