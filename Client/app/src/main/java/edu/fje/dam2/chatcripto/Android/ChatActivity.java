package edu.fje.dam2.chatcripto.Android;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import edu.fje.dam2.chatcripto.Models.AsymmetricEncryption;
import edu.fje.dam2.chatcripto.Models.DigitalSignature;
import edu.fje.dam2.chatcripto.Models.Message;
import edu.fje.dam2.chatcripto.Models.SymmetricEncryption;
import edu.fje.dam2.chatcripto.R;


/**
 * Android client which works as a chat client
 * <p>
 * If is used on the Android emulator, follow these steps:
 * <p>
 * On a terminal:
 * telnet localhost 5554
 * auth
 * redir add tcp:5050:8189
 * telnet localhost 8189
 */
public class ChatActivity extends AppCompatActivity {

    // UI components
    private RecyclerView messages;
    public RecyclerView.Adapter adapter;
    public RecyclerView.LayoutManager layoutManager;
    private Button buttonSend;
    private EditText keyboardInput;

    // Variables
    private com.github.nkzawa.socketio.client.Socket socket = null;
    private static final String url = "http://10.243.53.253";
    private static final String port = "8189";

    private List<Message> messageList = new ArrayList();
    private String nickname;
    private int joinCounter = 1;
    private int messagesCounter = 1;

    // Encryption variables
    private SecretKey symmetricSecretKey = null;
    private PrivateKey asymmetricPrivateKey = null;
    //private PublicKey asymmetricPublicKey = null;
    private PrivateKey signaturePrivateKey = null;
    private PublicKey signaturePublicKey = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        //Asking for username to the user
        // Input username on start a game
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Type a nickname for the chat session");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        alertDialog.setView(input);

        // Set up the buttons
        alertDialog.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                nickname = input.getText().toString();
                joinChat();
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Intent intent = new Intent(ChatActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        alertDialog.setCancelable(false);
        alertDialog.show();


        // Getting components from the layout
        messages = findViewById(R.id.messages);

        layoutManager = new LinearLayoutManager(this);
        messages.setLayoutManager(layoutManager);

        adapter = new MessagesAdapter(messageList);
        messages.setAdapter(adapter);

        buttonSend = findViewById(R.id.buttonSend);
        keyboardInput = findViewById(R.id.keyboardInput);

        // Send a message
        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    sendMessage();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    public void joinChat() {
        // Connecting to the chat server
        try {
            socket = IO.socket(url + ":" + port);

            socket.connect();
            socket.emit("join", nickname);

            socket.on("userjoinedthechat", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    joinCounter = 1;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (joinCounter == 1) {
                                String data = (String) args[0];
                                // get the extra data from the fired event and display a toast
                                Toast.makeText(ChatActivity.this, data, Toast.LENGTH_SHORT).show();

                                joinCounter++;
                            }
                        }
                    });
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public void sendMessage() throws JSONException {
        String msg = keyboardInput.getText().toString();
        keyboardInput.setText("");

        JSONObject msgJSON = new JSONObject();
        msgJSON.put("sender", nickname);
        msgJSON.put("msg", msg);

        socket.emit("send", msgJSON.toString());

        socket.on("newmessage", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                messagesCounter = 1;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (messagesCounter == 1) {
                                JSONObject msgObject = new JSONObject((String) args[0]);
                                String sender = msgObject.getString("sender");
                                String msg = msgObject.getString("msg");

                                configAllEncryption();

                                Message message = new Message(sender, msg, symmetricSecretKey, asymmetricPrivateKey, signaturePrivateKey);
                                messageList.add(message);

                                adapter = new MessagesAdapter(messageList);
                                adapter.notifyDataSetChanged();
                                messages.setAdapter(adapter);

                                int bottom = messages.getAdapter().getItemCount() - 1;
                                messages.smoothScrollToPosition(bottom);

                                messagesCounter++;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    public void configAllEncryption() throws Exception {
        // Symmetric encryption
        SymmetricEncryption symmetricEncryption = new SymmetricEncryption();
        this.symmetricSecretKey = (SecretKey) symmetricEncryption.getSecretKey();

        // Asymmetric encryption
        AsymmetricEncryption asymmetricEncryption = new AsymmetricEncryption();
        this.asymmetricPrivateKey = asymmetricEncryption.getPrivateKey();
        //this.asymmetricPublicKey = asymmetricEncryption.getPublicKey();

        // Digital signature
        DigitalSignature digitalSignature = new DigitalSignature();
        this.signaturePrivateKey = digitalSignature.getPrivateKey();
        this.signaturePublicKey = digitalSignature.getPublicKey();
    }


    public void disconnect() {
        socket.disconnect();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        disconnect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        disconnect();
    }

}



