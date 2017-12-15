package heatchat.unite.com.heatchat.ui.schools;

import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import heatchat.unite.com.heatchat.R;
import heatchat.unite.com.heatchat.adapters.SchoolListAdapter;
import heatchat.unite.com.heatchat.di.Injectable;
import heatchat.unite.com.heatchat.models.School;
import heatchat.unite.com.heatchat.viewmodel.SharedViewModel;

/**
 * A simple fragment that displays the list of schools.
 * Activities that contain this fragment must implement the
 * {@link SchoolListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SchoolListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SchoolListFragment extends Fragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @BindView(R.id.school_list_recycler)
    RecyclerView schoolList;

    private SchoolListViewModel schoolListViewModel;
    private SchoolListAdapter adapter = new SchoolListAdapter();
    private Unbinder unbinder;
    private SharedViewModel sharedViewModel;

    public SchoolListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment SchoolListFragment.
     */
    public static SchoolListFragment newInstance() {
        SchoolListFragment fragment = new SchoolListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new SchoolListAdapter();
        adapter.setClickListener(this::changeSchool);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        schoolListViewModel = ViewModelProviders.of(this, viewModelFactory)
                .get(SchoolListViewModel.class);
        sharedViewModel = ViewModelProviders.of(getActivity(), viewModelFactory)
                .get(SharedViewModel.class);
        schoolListViewModel.getSchools().observe(this, schools -> adapter.setSchools(schools));

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_school_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        schoolList.setLayoutManager(new LinearLayoutManager(getContext()));
        schoolList.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void changeSchool(School school) {
        sharedViewModel.select(school);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
