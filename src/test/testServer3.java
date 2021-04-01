package test;

import Server.Server;

import java.io.IOException;

public class testServer3 {
    public static void main(String []args) throws Exception {
        try {
            Server server1 = new Server(3);
            server1.test();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
