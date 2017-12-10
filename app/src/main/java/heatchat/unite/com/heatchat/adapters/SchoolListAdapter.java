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
 * Created by Andrew on 12/10/2017.
 */

public class SchoolListAdapter extends RecyclerView.Adapter<SchoolListAdapter.SchoolViewHolder> {

    private List<School> schools;

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
        holder.name.setText(schools.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return schools.size();
    }

    public void setSchools(List<School> schools) {
        this.schools = schools;
        notifyDataSetChanged();
    }

    static class SchoolViewHolder extends RecyclerView.ViewHolder {

        TextView name;

        SchoolViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.school_name);
        }
    }
}
