package colorfightII;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class WebsocketClientEndpoint {

    Session userSession = null;
    private MessageHandler messageHandler;

    public WebsocketClientEndpoint( URI endpointURI ) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer( this, endpointURI );
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    /**
     * Callback hook for Connection open events.
     *
     * @param userSession the userSession which is opened.
     */
    @OnOpen
    public void onOpen( Session userSession ) throws IOException {
        System.out.println( "opening websocket" );
        this.userSession = userSession;
        this.userSession.setMaxIdleTimeout(-1);
        final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    myTask();
                    System.out.println( "sent ping" );
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 30, 30, TimeUnit.SECONDS);

    }

    private void myTask() throws IOException {
        this.userSession.getAsyncRemote().sendPing( ByteBuffer.wrap( "ping server".getBytes() ) );
    }

    /**
     * Callback hook for Connection close events.
     *
     * @param userSession the userSession which is getting closed.
     * @param reason the reason for connection close
     */
    @OnClose
    public void onClose( Session userSession, CloseReason reason ) {
        System.out.println( "closing websocket" );
        this.userSession = null;
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a client send a message.
     *
     * @param message The text message
     */
    @OnMessage
    public void onMessage( String message ) {
        if ( this.messageHandler != null ) {
            this.messageHandler.handleMessage( message );
        }
    }

    /**
     * register message handler
     *
     * @param msgHandler
     */
    public void addMessageHandler( MessageHandler msgHandler ) {
        this.messageHandler = msgHandler;
    }

    /**
     * Send a message.
     *
     * @param message
     */
    public void sendMessage( String message ) {
        this.userSession.getAsyncRemote().sendText( message );
    }

    /**
     * Message handler.
     */
    public static interface MessageHandler {

        public void handleMessage( String message );
    }
}
