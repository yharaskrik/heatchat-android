package heatchat.unite.com.heatchat.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import heatchat.unite.com.heatchat.R;
import heatchat.unite.com.heatchat.models.School;

/**
 * A simple RecyclerView adapter that displays a list of schools.
 * <p>
 * Uses a simple click listener to listen to the click event on a item. Set the {@link
 * SchoolClickListener} with {@link #setClickListener(SchoolClickListener)} to listen on which
 * school was clicked.
 * <p>
 * Created by Andrew on 12/10/2017.
 */

public class SchoolListAdapter extends RecyclerView.Adapter<SchoolListAdapter.SchoolViewHolder> {

    private List<School> schools;

    private SchoolClickListener mClickListener;

    public SchoolListAdapter() {
        schools = new ArrayList<>();
    }

    @Override
    public SchoolViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SchoolViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(SchoolViewHolder holder, int position) {
        final School school = schools.get(position);
        holder.name.setText(school.getName());
        holder.name.setOnClickListener(view -> {
            if (mClickListener != null) {
                mClickListener.onClick(school);
            }
        });
    }

    @Override
    public int getItemCount() {
        return schools.size();
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
        notifyDataSetChanged();
    }

    public void setClickListener(
            SchoolClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }

    public interface SchoolClickListener {
        void onClick(School school);
    }

    static class SchoolViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        SchoolViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.school_name);
        }
    }
}
