import com.demo.grpc.User;
import com.demo.grpc.userGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import javax.jws.soap.SOAPBinding;
import java.util.Scanner;
import java.util.logging.Logger;

public class GrpcClient {


    private static final Logger logger = Logger.getLogger(GrpcClient.class.getName());

    public static void main(String[] args) {
        //connection with server
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost" , 9090).usePlaintext().build() ;
        userGrpc.userBlockingStub userBlockingStub = new userGrpc.userBlockingStub(managedChannel) ;

        //Login activation
        boolean auth = false ;
        Scanner input = new Scanner(System.in) ;
        while(!auth){
            System.out.println("Enter Username : ");
            String name = input.next() ;
            System.out.println("Enter Password : ");
            String password = input.next() ;

            User.LoginReq loginReq = User.LoginReq.newBuilder().setUsername(name).setPassword(password).build() ;

            User.APIRes apiRes = userBlockingStub.login(loginReq) ;
            if(apiRes.getResCode() == 200){
                auth = true ;
            }
            logger.info(apiRes.getMessage());
        }

        //create new user
        System.out.println("Enter registration Id : ");
        long ID = input.nextLong() ;
        System.out.println("Enter Username");
        String name = input.next() ;

        User.RegisterRequest registerRequest = User.RegisterRequest.newBuilder().
                setRegistrationID(ID).setUsername(name).build() ;
        User.RegResponse regResponse = userBlockingStub.register(registerRequest);
        logger.info(regResponse.getResponse());

    }
}
