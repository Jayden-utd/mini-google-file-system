package test;

import Server.Server;

import java.io.IOException;

public class testServer4 {
    public static void main(String []args) throws Exception {
        try {
            Server server1 = new Server(4);
            server1.test();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
