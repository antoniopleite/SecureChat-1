package SecureChat;

import java.lang.Thread;
import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import com.google.gson.stream.*;

class ServerActions implements Runnable {

ClientDescription me;
boolean registered = false;

Socket client;
JsonReader in;
OutputStream out;
ServerControl registry;

ServerActions ( Socket c, ServerControl r )
{
    client = c;
    registry = r;

    try {
	in = new JsonReader( new InputStreamReader ( c.getInputStream(), "UTF-8") );
	out = c.getOutputStream();
    } catch ( Exception e ) {
        System.err.print( "Cannot use client socket: " + e );
	Thread.currentThread().interrupt();
    }
}

JsonObject
readCommand ()
{
    try {
	JsonElement data = new JsonParser().parse( in );
	if (data.isJsonObject()) {
	    return data.getAsJsonObject();
	}
        System.err.print ( "Error while reading command from socket (not a JSON object), connection will be shutdown\n" );
	return null;
    } catch (Exception e) {
        System.err.print ( "Error while reading JSON command from socket, connection will be shutdown\n" );
	return null;
    }

}

void
sendResult ( String error, String extra )
{
     String msg = "{\"error\":\"" + error + "\"";

     if (extra != null) {
         msg += "," + extra;
     }
     msg += "}\n";

     try {
	 System.out.print( "Send result: " + msg );
	 out.write ( msg.getBytes( StandardCharsets.UTF_8 ) );
     } catch (Exception e ) {}
}

void
executeCommand ( JsonObject data )
{
     JsonElement cmd = data.get( "command" );

     if (cmd != null && cmd.getAsString().equals( "register" )) {
         JsonElement id = data.get( "src" );
	 if (id == null) {
	     System.err.print ( "No \"src\" field in \"register\" command: " + data );
	     sendResult( "unknown", null );
	     return;
	 }

	 if (registered || registry.clientExists( id.getAsString() )) {
	     System.err.println ( "Client is already registered: " + data );
	     if (registered) {
		 sendResult( "re-registered", null );
	     }
	     else {
		 sendResult( "registered", null );
	     }
	     return;
	 }

 
	 data.remove ( "command" );
	 me = registry.addClient( id.getAsString(), data, out );
	 registered = true;

	 sendResult( "ok", null );
	 return;
     }
     else if (cmd != null && cmd.getAsString().equals( "list" )) {
         JsonElement id = data.get( "id" );
	 String list = registry.listClients( id == null ? null : id.getAsString() );

	 if (id != null && list == null) {
	     sendResult ( "unknown", null );
	 }
	 else {
	     if (list == null) {
		 sendResult ( "ok", "\"result\":[]" );
	     }
	     else {
		 sendResult ( "ok", "\"result\":" + list );
	     }
	 }
	 return;
     }
     else if (cmd != null && cmd.getAsString().equals( "send" )) {
         JsonElement id = data.get( "dst" );
	 OutputStream[] list = registry.getOutputStreams( id == null ? null : id.getAsString() );

         if (list == null) {
	     sendResult ( "unknown", null );
	     return;
	 }

	 data.remove( "command" );

	 for (int i = 0; i < list.length; i++) {
	     if (me != null && list[i] == me.out) { // Don't sendmsg to myself
	         continue;
	     }

	     try {
		 System.out.println ( "send msg \"" + data.toString() + "\"" );
		 list[i].write( data.toString().getBytes( StandardCharsets.UTF_8 ) );
	     } catch (Exception e) {}
	 }

	 sendResult ( "ok", null );
	 return;
     }
     else {
         System.err.println ( "Invalid command in message: " + data );
	 return;
     }
}

public void
run ()
{
    while (true) {
        JsonObject cmd = readCommand();
	if (cmd == null) {
	    if (registered) {
	        registry.removeClient( me.id );
		try {
		    client.close();
		} catch (Exception e) {}
		return;
	    }
	}
	executeCommand ( cmd );
    }

}

}

