package services;

import com.demo.grpc.User;
import com.demo.grpc.userGrpc;
import io.grpc.stub.StreamObserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import static java.sql.DriverManager.getConnection;

public class UserService extends userGrpc.userImplBase {
    //MySQL info
    String url = "jdbc:mysql://localhost:3306/student_registration";
    String user = "root";
    String pass = "";
    private static final Logger logger = Logger.getLogger(UserService.class.getName());
    @Override
    public void login(User.LoginReq request, StreamObserver<User.APIRes> responseObserver) throws SQLException, ClassNotFoundException {

        String userName = request.getUsername();
        String password = request.getPassword();

        ResultSet resultSet = checkLoginInfo(userName, password);

        logger.info("Request generated from user : " + userName);

        User.APIRes.Builder response = User.APIRes.newBuilder();
        while(resultSet.next()) {
            if (resultSet.getInt(1) == 1) {
                response.setResCode(200).setMessage("SUCCESS");
                logger.info("Login successful for user : " + userName);
            } else {
                response.setResCode(400).setMessage("BAD REQUEST");
                logger.info("Login failed for user : " + userName);
            }
            break;
        }


        responseObserver.onNext(response.build());
        responseObserver.onCompleted();
    }

    private ResultSet checkLoginInfo(String userName, String password) throws ClassNotFoundException, SQLException, SQLException {
        //Connecting to MySQL database
        Class.forName("com.mysql.cj.jdbc.Driver");
       Connection connection = getConnection(url,user,pass) ;
        PreparedStatement statement = connection.prepareStatement("SELECT EXISTS(SELECT * FROM login_info" +
                " WHERE username = ? && pass = ?)");
        statement.setString(1, userName);
        statement.setString(2, password);
        ResultSet rs = statement.executeQuery();
        return rs;
    }
    @Override
    public void register(User.RegisterRequest request, StreamObserver<User.RegResponse> responseObserver) throws SQLException {
        long regID = request.getRegistrationID();
        String studentName = request.getUsername() ;

        //Checking database
        ResultSet resultSet = checkRegInfo(regID);

        //Creating response
        User.RegResponse.Builder regResponse = new User.RegResponse.Builder();
        while(resultSet.next()){
            if(resultSet.getInt(1) == 1){
                regResponse
                        .setResponse("Registration ID " + regID + " is already registered")
                        .setResponseCode(500);
            }else{
                Connection connection = getConnection(url, user, pass);
                //Adding new student
                PreparedStatement statement = connection.prepareStatement
                        ("INSERT INTO registration_list VALUES('"+regID+"', '"+studentName+"')");
                statement.executeUpdate();
                regResponse.setResponse(studentName +
                        " with registration ID " + regID + " is now registered successfully").
                        setResponseCode(300);
            }
            break;
        }
        responseObserver.onNext(regResponse.build());
        responseObserver.onCompleted();
    }

    private ResultSet checkRegInfo(long regID) throws SQLException {
        //Connecting to MySQL database
        Connection connection = getConnection(url, user, pass);
        PreparedStatement statement = connection.prepareStatement
                ("SELECT EXISTS(SELECT * FROM registration_list WHERE Reg_ID = ?)");
        statement.setInt(1, (int) regID);
        ResultSet rs = statement.executeQuery();
        return rs;
    }

    @Override
    public void logout(User.empty request, StreamObserver<User.APIRes> responseObserver) {
        super.logout(request, responseObserver);
    }
}

