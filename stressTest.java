import java.io.IOException;

public class stressTest {
    public static void main(String[] args) throws IOException, InterruptedException{
        for(int i = 0; i < 1000; i++){
            String username = "user" + Integer.toString(i);
            Peer p = new Peer();
            p.registerTracker(username);
            p.sendMessage("Hello");
            Thread.sleep(100);
        }
        
    }
}
