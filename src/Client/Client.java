package Client;

import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.OutputStream;
import java.io.InputStream;

class Client {

public static void main ( String[] args )
{
    Socket s;
    OutputStream out;
    InputStream in;
    byte[] buffer = new byte[1024];
    
    try {
	s = new Socket( args[0], Integer.parseInt( args[1] ) );
	out = s.getOutputStream();
	in = s.getInputStream();

	while (true) {
	    int l;
	    if (System.in.available() != 0) {
		l = System.in.read( buffer );
		if (l == -1) break;

		out.write( buffer, 0, l );
	    }

	    if (in.available() != 0) {
		l = in.read( buffer, 0, buffer.length );
		System.out.write( buffer, 0, l );
		System.out.print( "\n" );
	    }

	    Thread.currentThread().sleep( 200 ); // 100 milis
	}
    } catch (Exception e) {
        System.err.println( "Exception: " + e );
    }
}

}
