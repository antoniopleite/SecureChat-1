package Client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.OutputStream;
import java.io.InputStream;

class Client {

    public static void main(String[] args) {
        Socket s;
        OutputStream out;
        InputStream in;
        byte[] buffer = new byte[1024];

        try {
            s = new Socket("localhost",1111 );
            out = s.getOutputStream();
            in = s.getInputStream();

            while (true) {
                int l;
                if (System.in.available() != 0) {
                    l = System.in.read(buffer);
                    if (l == -1) break;
                    try {
                        buffer = encryptedMsg(buffer);
                    }catch(JsonSyntaxException ex){
                        System.out.println("Ta mal");
                    }

                    //in = new JsonReader(new InputStreamReader(buffer, "UTF-8"));
                    out.write(buffer, 0, l);

                }

                if (in.available() != 0) {
                    l = in.read(buffer, 0, buffer.length);
                    System.out.write(buffer, 0, l);
                    System.out.print("\n");
                }

                Thread.currentThread().sleep(200); // 100 milis
            }
        } catch (Exception e) {
            System.err.println("Exception: " + e);
        }
    }

    private static byte[] encryptedMsg(byte[] buff) throws JsonSyntaxException{
        System.out.println("1");
        byte[] buffinal = null;
        System.out.println("2");
        String buffTemp = new String(buff);
        System.out.println(buffTemp);
        JsonParser parser = new JsonParser();
        JsonElement data = parser.parse(buffTemp);
        System.out.println("3");
        if (data.isJsonObject()) {
            JsonObject json = data.getAsJsonObject();
            if(json.has("msg")) {
                System.out.println("aqui");
                JsonElement cmd = json.get("msg");
                String msg = cmd.getAsString();
                System.out.println(msg);
            }
        }

        return buffinal;
    }

}
