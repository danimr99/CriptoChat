package edu.fje.dam2.chatcripto.Android;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.fje.dam2.chatcripto.Models.Message;
import edu.fje.dam2.chatcripto.R;


public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MyViewHolder> {
    private List<Message> messageList;

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView senderID, message;

        public MyViewHolder(View v) {
            super(v);

            senderID = v.findViewById(R.id.sender_id);
            message = v.findViewById(R.id.msg);
        }
    }

    public MessagesAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public MessagesAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message, parent, false);

        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Message item = messageList.get(position);

        holder.senderID.setText(item.getSender());
        holder.message.setText(item.getMessage());

        holder.message.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (item.isLongPressed()) {
                    holder.message.setText(item.getEncryptionText());
                    item.setLongPressed(false);
                } else {
                    holder.message.setText(item.getMessage());
                    item.setLongPressed(true);
                }
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.messageList.size();
    }
}