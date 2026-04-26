package com.example.nearneed;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class AiChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<AiChatMessage> messages;

    public AiChatAdapter(List<AiChatMessage> messages) {
        this.messages = messages;
    }

    @Override public int getItemViewType(int position) { return messages.get(position).getType(); }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == AiChatMessage.TYPE_USER) {
            return new UserVH(inf.inflate(R.layout.item_ai_chat_user, parent, false));
        } else {
            return new BotVH(inf.inflate(R.layout.item_ai_chat_bot, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AiChatMessage msg = messages.get(position);

        if (holder instanceof UserVH) {
            UserVH uvh = (UserVH) holder;
            // Image
            if (msg.hasImage()) {
                uvh.ivImage.setVisibility(View.VISIBLE);
                Glide.with(uvh.ivImage.getContext())
                        .load(msg.getImageUri())
                        .centerCrop()
                        .into(uvh.ivImage);
            } else {
                uvh.ivImage.setVisibility(View.GONE);
            }
            // Text
            if (msg.getText() != null && !msg.getText().isEmpty()) {
                uvh.tvMessage.setVisibility(View.VISIBLE);
                uvh.tvMessage.setText(msg.getText());
            } else {
                uvh.tvMessage.setVisibility(msg.hasImage() ? View.GONE : View.VISIBLE);
                uvh.tvMessage.setText(msg.getText());
            }

        } else if (holder instanceof BotVH) {
            BotVH bvh = (BotVH) holder;
            if (msg.isTyping()) {
                bvh.tvMessage.setVisibility(View.GONE);
                bvh.llTyping.setVisibility(View.VISIBLE);
                animateDot(bvh.dot1, 0);
                animateDot(bvh.dot2, 150);
                animateDot(bvh.dot3, 300);
            } else {
                bvh.tvMessage.setVisibility(View.VISIBLE);
                bvh.llTyping.setVisibility(View.GONE);
                bvh.tvMessage.setText(msg.getText());
            }
        }
    }

    private void animateDot(View dot, long delay) {
        ObjectAnimator anim = ObjectAnimator.ofFloat(dot, "translationY", 0f, -8f, 0f);
        anim.setDuration(600);
        anim.setStartDelay(delay);
        anim.setRepeatCount(ObjectAnimator.INFINITE);
        anim.start();
    }

    @Override public int getItemCount() { return messages.size(); }

    static class UserVH extends RecyclerView.ViewHolder {
        TextView  tvMessage;
        ImageView ivImage;
        UserVH(@NonNull View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
            ivImage   = v.findViewById(R.id.ivImage);
        }
    }

    static class BotVH extends RecyclerView.ViewHolder {
        TextView tvMessage;
        View     llTyping, dot1, dot2, dot3;
        BotVH(@NonNull View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvMessage);
            llTyping  = v.findViewById(R.id.llTyping);
            dot1      = v.findViewById(R.id.dot1);
            dot2      = v.findViewById(R.id.dot2);
            dot3      = v.findViewById(R.id.dot3);
        }
    }
}
