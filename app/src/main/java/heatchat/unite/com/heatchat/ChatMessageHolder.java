package heatchat.unite.com.heatchat;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jaybell on 02/11/17.
 */

public class ChatMessageHolder extends RecyclerView.ViewHolder {
    View mView;

    private TextView text;

    public ChatMessageHolder(View itemView) {
        super(itemView);
        mView = itemView;
    }

    public ChatMessageHolder(View itemView, String text) {
        super(itemView);
        mView = itemView;
        this.text.setText(text);
    }

    public void setText(TextView textView, String text) {
        this.text = textView;
        this.text.setText(text);
    }

}