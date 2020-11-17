package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @ClassName UserInputHandler
 * @Author DiangD
 * @Date 2020/11/18
 * @Version 1.0
 * @Description
 **/
public class UserInputHandler implements Runnable {
    private ChatClient chatClient;

    public UserInputHandler(ChatClient chatClient) {
        this.chatClient = chatClient;
    }


    @Override
    public void run() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = reader.readLine();

                chatClient.send(msg);

                if (chatClient.readyToQuit(msg)){
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
