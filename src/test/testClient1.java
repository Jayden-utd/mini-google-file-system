package test;

import Client.Client;

import java.util.Map;

public class testClient1 {
    public static void main(String []args) throws Exception {
            Client c1= new Client(1);
            try {
                c1.test();
            } catch (Exception e) {
                e.printStackTrace();
            }

    }

}
