package com.example.touristinrussia;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class InfoFragment extends Fragment {
    private int pageNumber;

    public static InfoFragment newInstance(int page) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putInt("num", page);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments() != null ? getArguments().getInt("num") : 1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = null;
        if (pageNumber == 0) {
            result = inflater.inflate(R.layout.about_app, container, false);
        }
        else if (pageNumber == 1) {
            result = inflater.inflate(R.layout.about_author, container, false);
        }
        else if (pageNumber == 2) {
            result = inflater.inflate(R.layout.instruction, container, false);
        }
        return result;
    }
}