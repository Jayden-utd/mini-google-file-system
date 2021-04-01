package test;

import Server.Server;

import java.io.IOException;

public class testServer5 {
    public static void main(String []args) throws Exception {
        try {
            Server server1 = new Server(5);
            server1.test();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
