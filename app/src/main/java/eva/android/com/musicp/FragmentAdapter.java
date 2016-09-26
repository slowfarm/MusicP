package eva.android.com.musicp;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import eva.android.com.musicp.R;


public class FragmentAdapter extends RecyclerView.Adapter<FragmentAdapter.ViewHolder> {

    private ArrayList<String> label;
    private ArrayList<String> author;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView labelText;
        public TextView authorText;

        public ViewHolder(View v) {
            super(v);
            labelText = (TextView) v.findViewById(R.id.labelText);
            authorText = (TextView) v.findViewById(R.id.authorText);
        }
    }

    public FragmentAdapter(ArrayList<String> label, ArrayList<String> author) {
        this.label = label;
        this.author = author;

    }

    @Override
    public FragmentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                         int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.songlistitem, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.labelText.setText(label.get(position));
        holder.authorText.setText(author.get(position));

    }

    @Override
    public int getItemCount() {
        return label.size();
    }

}